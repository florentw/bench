/*
 * Copyright 2016 Florent Weber <florent.weber@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.amaze.bench.runtime.actor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import io.amaze.bench.runtime.agent.AgentKey;
import io.amaze.bench.runtime.cluster.ClusterConfigFactory;
import io.amaze.bench.shared.util.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.StandardSystemProperty.JAVA_CLASS_PATH;
import static com.google.common.base.StandardSystemProperty.JAVA_HOME;
import static com.google.common.util.concurrent.Uninterruptibles.joinUninterruptibly;
import static io.amaze.bench.runtime.agent.Constants.LOG_DIRECTORY_NAME;

/**
 * Forks a new JVM to host the actorKey.<br>
 * The main class that will be spawn is {@link ActorBootstrap}.<br>
 * <p>
 * A watchdog thread waits for the process termination to avoid zombies.
 *
 * @see ActorBootstrap
 */
final class ForkedActorManager extends AbstractActorManager implements ProcessTerminationListener {

    private static final Logger log = LogManager.getLogger();

    private static final String JAVA_CMD_PATH = File.separator + "bin" + File.separator + "java";
    private static final String TMP_CONFIG_PREFIX = "config-";
    private static final String TMP_CONFIG_SUFFIX = ".json";

    private final ClusterConfigFactory clusterConfigFactory;
    private final Map<ActorKey, ProcessWatchDogThread> processes = new HashMap<>();
    private final File localLogDir;

    @VisibleForTesting
    ForkedActorManager(@NotNull final AgentKey agent,
                       @NotNull final ClusterConfigFactory clusterConfigFactory,
                       @NotNull final File localLogDir) {
        super(agent);
        this.clusterConfigFactory = checkNotNull(clusterConfigFactory);
        this.localLogDir = checkNotNull(localLogDir);

        boolean success = localLogDir.mkdir();
        if (!success && !localLogDir.exists()) {
            throw new IllegalStateException("Could not create directory: " + localLogDir);
        }
    }

    ForkedActorManager(@NotNull final AgentKey agent, @NotNull final ClusterConfigFactory clusterConfigFactory) {
        this(agent, clusterConfigFactory, new File(LOG_DIRECTORY_NAME));
    }

    @NotNull
    @Override
    public ManagedActor createActor(@NotNull final ActorConfig actorConfig) throws ValidationException {
        checkNotNull(actorConfig);

        final ActorKey actor = actorConfig.getKey();

        synchronized (processes) {
            if (processes.containsKey(actor)) {
                throw new IllegalArgumentException("An actorKey with the name " + actor + " already exists.");
            }
        }

        Process process = createActorProcess(actorConfig, clusterConfigFactory.clusterConfigFor(actor));

        ProcessWatchDogThread watchDog = new ProcessWatchDogThread(actor.getName(), process, this);
        watchDog.start();
        watchDog.awaitUntilStarted();

        synchronized (processes) {
            if (processes.containsKey(actor)) {
                throw new IllegalArgumentException("An actorKey with the name " + actor + " already exists.");
            }

            processes.put(actor, watchDog);
        }

        return new ForkedManagedActor(actor);
    }

    @Override
    public void close() {
        Set<ProcessWatchDogThread> threads;
        synchronized (processes) {
            threads = ImmutableSet.copyOf(processes.values());
            processes.clear();
        }

        threads.forEach(this::terminateProcess);
    }

    @Override
    public void onProcessExited(@NotNull final String actorName, @NotNull final int exitCode) {
        checkNotNull(actorName);
        log.info("Forked actor {} exited with code {}.", actorName, exitCode);
        removeFromProcesses(new ActorKey(actorName));
    }

    @VisibleForTesting
    Map<ActorKey, ProcessWatchDogThread> getProcesses() {
        synchronized (processes) {
            return ImmutableMap.copyOf(processes);
        }
    }

    private Process createActorProcess(final ActorConfig actorConfig, final Config clusterConfig) {
        try {
            File tempActorConfig = writeTmpFile(actorConfig.getActorJsonConfig());
            File tempClusterConfig = writeTmpFile(clusterConfig.root().render(ConfigRenderOptions.concise()));

            return forkProcess(actorConfig, tempActorConfig.getAbsolutePath(), tempClusterConfig.getAbsolutePath());

        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private File writeTmpFile(final String actorJsonConfig) throws IOException {
        File tempActorConfig = File.createTempFile(TMP_CONFIG_PREFIX, TMP_CONFIG_SUFFIX);
        Files.writeTo(tempActorConfig, actorJsonConfig);
        return tempActorConfig;
    }

    private void terminateProcess(final ProcessWatchDogThread thread) {
        thread.close();
        thread.getProcess().destroy();
        joinUninterruptibly(thread);
    }

    private Process forkProcess(@NotNull final ActorConfig actorConfig,
                                @NotNull final String actorConfigFile,
                                @NotNull final String clusterConfigFile) throws IOException {

        String name = actorConfig.getKey().getName();

        String[] cmd = { //
                JAVA_HOME.value() + JAVA_CMD_PATH, //
                "-cp", //
                JAVA_CLASS_PATH.value(), // Use the current classpath
                ActorBootstrap.class.getName(), // Main class
                name,                           // arg[0]
                actorConfig.getClassName(),     // arg[1]
                clusterConfigFile,              // arg[2]
                actorConfigFile                 // arg[3]
        };

        String actorLogFileName = localLogDir.getAbsolutePath() + File.separator + name + ".log";
        File actorLogFile = new File(actorLogFileName);

        ProcessBuilder builder = new ProcessBuilder(cmd) //
                .redirectErrorStream(true) //
                .redirectOutput(actorLogFile);

        log.info("Started process for {} with command {}, logging to {}", name, builder.command(), actorLogFileName);

        return builder.start();
    }

    private ProcessWatchDogThread removeFromProcesses(ActorKey actor) {
        ProcessWatchDogThread thread;
        synchronized (processes) {
            thread = processes.remove(actor);
            if (thread == null) {
                return null;
            }
        }
        return thread;
    }

    private final class ForkedManagedActor implements ManagedActor {
        private final ActorKey actorKey;

        ForkedManagedActor(final ActorKey key) {
            this.actorKey = key;
        }

        @NotNull
        @Override
        public ActorKey getKey() {
            return actorKey;
        }

        @Override
        public void close() {
            ProcessWatchDogThread watchDogThread = removeFromProcesses(actorKey);
            if (watchDogThread != null) {
                terminateProcess(watchDogThread);
            }
        }
    }

}
