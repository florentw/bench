/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
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
import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.ClusterConfigFactory;
import io.amaze.bench.cluster.actor.ActorConfig;
import io.amaze.bench.cluster.actor.ValidationException;
import io.amaze.bench.shared.util.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.StandardSystemProperty.JAVA_CLASS_PATH;
import static com.google.common.base.StandardSystemProperty.JAVA_HOME;
import static com.google.common.util.concurrent.Uninterruptibles.joinUninterruptibly;
import static io.amaze.bench.cluster.agent.Constants.LOG_DIRECTORY_NAME;
import static java.util.Objects.requireNonNull;

/**
 * Forks a new JVM to host the actorKey.<br>
 * The main class that will be spawn is {@link ActorBootstrap}.<br>
 * <p>
 * A watchdog thread waits for the process termination to avoid zombies.
 *
 * @see ActorBootstrap
 */
final class ForkedActorManager implements ActorManager, ProcessTerminationListener {

    private static final Logger log = LogManager.getLogger();

    private static final String JAVA_CMD_PATH = File.separator + "bin" + File.separator + "java";
    private static final String TMP_CONFIG_PREFIX = "config-";
    private static final String TMP_CONFIG_SUFFIX = ".json";
    private static final String CLASSPATH_ENV = "CLASSPATH";

    private final ClusterConfigFactory clusterConfigFactory;
    private final Map<ActorKey, ProcessWatchDogThread> processes = new HashMap<>();
    private final File localLogDir;

    @VisibleForTesting
    ForkedActorManager(@NotNull final ClusterConfigFactory clusterConfigFactory, @NotNull final File localLogDir) {
        super();
        this.clusterConfigFactory = requireNonNull(clusterConfigFactory);
        this.localLogDir = requireNonNull(localLogDir);

        boolean success = localLogDir.mkdir();
        if (!success && !localLogDir.exists()) {
            throw new IllegalStateException("Could not create directory: " + localLogDir);
        }
    }

    ForkedActorManager(@NotNull final ClusterConfigFactory clusterConfigFactory) {
        this(clusterConfigFactory, new File(LOG_DIRECTORY_NAME));
    }

    @NotNull
    @Override
    public ManagedActor createActor(@NotNull final ActorConfig actorConfig) throws ValidationException {
        requireNonNull(actorConfig);

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
        requireNonNull(actorName);
        log.info("Forked actor {} exited with code {}.", actorName, exitCode);
        removeFromProcesses(new ActorKey(actorName));
    }

    @VisibleForTesting
    Map<ActorKey, ProcessWatchDogThread> getProcesses() {
        synchronized (processes) {
            return ImmutableMap.copyOf(processes);
        }
    }

    private static File writeTmpFile(final String actorJsonConfig) throws IOException {
        File tempActorConfig = File.createTempFile(TMP_CONFIG_PREFIX, TMP_CONFIG_SUFFIX);
        Files.writeTo(tempActorConfig, actorJsonConfig);
        return tempActorConfig;
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

    private void terminateProcess(final ProcessWatchDogThread thread) {
        thread.close();
        thread.getProcess().destroy();
        joinUninterruptibly(thread);
    }

    private Process forkProcess(@NotNull final ActorConfig actorConfig,
                                @NotNull final String actorConfigFile,
                                @NotNull final String clusterConfigFile) throws IOException {

        Files.checkFilePath(clusterConfigFile);
        Files.checkFilePath(actorConfigFile);

        String name = actorConfig.getKey().getName();

        List<String> command = createCommand(actorConfig, actorConfigFile, clusterConfigFile);

        String actorLogFileName = localLogDir.getAbsolutePath() + File.separator + name + ".log";
        File actorLogFile = new File(Files.checkFilePath(actorLogFileName));

        ProcessBuilder builder = new ProcessBuilder(command) //
                .redirectErrorStream(true) //
                .redirectOutput(actorLogFile);

        // Use the current classpath, passed as an env variable to avoid cluttering ps output.
        builder.environment().put(CLASSPATH_ENV, JAVA_CLASS_PATH.value());

        log.info("Started process for {} with command {}, logging to {}", name, builder.command(), actorLogFileName);

        return builder.start();
    }

    private List<String> createCommand(final ActorConfig actorConfig,
                                       final String actorConfigFile,
                                       final String clusterConfigFile) {

        List<String> tokens = new ArrayList<>();
        tokens.add(JAVA_HOME.value() + JAVA_CMD_PATH);// Using current JAVA_HOME for the new JVM
        tokens.addAll(actorConfig.getDeployConfig().getJvmArguments()); // Custom JVM args if any
        tokens.add(ActorBootstrap.class.getName());   // Main class
        tokens.add(actorConfig.getKey().getName());   // arg[0]
        tokens.add(actorConfig.getClassName());       // arg[1]
        tokens.add(clusterConfigFile);                // arg[2]
        tokens.add(actorConfigFile);                  // arg[3]
        return tokens;
    }

    private ProcessWatchDogThread removeFromProcesses(ActorKey actor) {
        synchronized (processes) {
            ProcessWatchDogThread thread = processes.remove(actor);
            if (thread == null) {
                return null;
            }
            return thread;
        }
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
