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
package io.amaze.bench.client.runtime.actor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.amaze.bench.shared.jms.JMSEndpoint;
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
import static io.amaze.bench.client.runtime.agent.Constants.LOG_DIRECTORY_NAME;

/**
 * Forks a new JVM to host the actor.<br>
 * The main class that will be spawn is {@link ActorBootstrap}.<br>
 * <p>
 * A watchdog thread waits for the process termination to avoid zombies.
 * <p>
 * Created on 3/13/16.
 *
 * @see ActorBootstrap
 */
final class ForkedActorManager extends AbstractActorManager implements ProcessTerminationListener {

    private static final Logger LOG = LogManager.getLogger(ForkedActorManager.class);

    private static final String JAVA_CMD_PATH = File.separator + "bin" + File.separator + "java";
    private static final String TMP_CONFIG_FILE_PREFIX = "actor-config";
    private static final String TMP_CONFIG_FILE_SUFFIX = ".json";

    private final Map<String, ProcessWatchDogThread> processes = new HashMap<>();
    private final File localLogDir;
    private final JMSEndpoint masterEndpoint;

    @VisibleForTesting
    ForkedActorManager(@NotNull final String agent,
                       @NotNull final JMSEndpoint masterEndpoint,
                       @NotNull final File localLogDir) {
        super(agent);
        this.masterEndpoint = checkNotNull(masterEndpoint);
        this.localLogDir = checkNotNull(localLogDir);

        boolean success = localLogDir.mkdir();
        if (!success && !localLogDir.exists()) {
            throw new IllegalStateException("Could not create directory: " + localLogDir);
        }
    }

    ForkedActorManager(@NotNull final String agent, @NotNull final JMSEndpoint masterEndpoint) {
        this(agent, masterEndpoint, new File(LOG_DIRECTORY_NAME));
    }

    @NotNull
    @Override
    public ManagedActor createActor(@NotNull final ActorConfig actorConfig) throws ValidationException {
        checkNotNull(actorConfig);

        final String actor = actorConfig.getName();
        Process process = createActorProcess(actorConfig);

        ProcessWatchDogThread watchDog = new ProcessWatchDogThread(actor, process, this);
        watchDog.start();
        watchDog.awaitUntilStarted();

        synchronized (processes) {
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
    public void onProcessExited(@NotNull final String actor, @NotNull final int exitCode) {
        checkNotNull(actor);
        LOG.info("Forked actor {} exited with code {}.", actor, exitCode);
        removeFromProcesses(actor);
    }

    @VisibleForTesting
    Map<String, ProcessWatchDogThread> getProcesses() {
        synchronized (processes) {
            return ImmutableMap.copyOf(processes);
        }
    }

    private Process createActorProcess(final ActorConfig actorConfig) {
        try {
            File tempConfigFile = File.createTempFile(TMP_CONFIG_FILE_PREFIX, TMP_CONFIG_FILE_SUFFIX);
            Files.writeTo(tempConfigFile, actorConfig.getActorJsonConfig());

            return forkProcess(actorConfig, tempConfigFile.getAbsolutePath());

        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private void terminateProcess(final ProcessWatchDogThread thread) {
        thread.close();
        thread.getProcess().destroy();
        joinUninterruptibly(thread);
    }

    private Process forkProcess(@NotNull final ActorConfig actorConfig, @NotNull final String configFileName)
            throws IOException {

        String name = actorConfig.getName();

        String[] cmd = { //
                JAVA_HOME.value() + JAVA_CMD_PATH, //
                "-cp", //
                JAVA_CLASS_PATH.value(), // Use the current classpath
                ActorBootstrap.class.getName(),             // Main class
                name,                                       // arg[0]
                actorConfig.getClassName(),                 // arg[1]
                masterEndpoint.getHost(),                   // arg[2]
                Integer.toString(masterEndpoint.getPort()), // arg[3]
                configFileName                              // arg[4]
        };

        String actorLogFileName = localLogDir.getAbsolutePath() + File.separator + name + ".log";
        File actorLogFile = new File(actorLogFileName);

        ProcessBuilder builder = new ProcessBuilder(cmd) //
                .redirectErrorStream(true) //
                .redirectOutput(actorLogFile);

        LOG.info("Started process with command {}, logging to {}", builder.command(), actorLogFileName);

        return builder.start();
    }

    private ProcessWatchDogThread removeFromProcesses(String actor) {
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
        private final String actor;

        ForkedManagedActor(final String actor) {
            this.actor = actor;
        }

        @NotNull
        @Override
        public String getName() {
            return actor;
        }

        @Override
        public void close() {
            ProcessWatchDogThread watchDogThread = removeFromProcesses(actor);
            if (watchDogThread != null) {
                terminateProcess(watchDogThread);
            }
        }
    }

}
