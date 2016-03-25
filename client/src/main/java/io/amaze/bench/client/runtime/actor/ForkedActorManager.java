package io.amaze.bench.client.runtime.actor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Uninterruptibles;
import io.amaze.bench.shared.helper.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.amaze.bench.client.runtime.agent.Constants.LOG_DIRECTORY_NAME;

/**
 * Forks a new JVM to host the actor.<br/>
 * The main class that will be spawn is {@link ActorBootstrap}.<br/>
 * <p>
 * A watchdog thread waits for the process termination to avoid zombies.
 * <p>
 * Created on 3/13/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 * @see ActorBootstrap
 */
final class ForkedActorManager extends AbstractActorManager {

    private static final Logger LOG = LoggerFactory.getLogger(ForkedActorManager.class);

    private static final String JAVA_CMD_PATH = File.separator + "bin" + File.separator + "java";
    private static final String TMP_CONFIG_FILE_PREFIX = "actor-config";
    private static final String TMP_CONFIG_FILE_SUFFIX = ".json";

    private final Map<String, WatchDogThread> processes = new ConcurrentHashMap<>();
    private final File localLogDir;

    ForkedActorManager(@NotNull final ActorFactory factory, @NotNull File localLogDir) {
        super(factory);
        this.localLogDir = localLogDir;

        boolean success = localLogDir.mkdir();
        if (!success && !localLogDir.exists()) {
            throw new IllegalStateException("Could not create directory: " + localLogDir);
        }
    }

    ForkedActorManager(@NotNull final ActorFactory factory) {
        this(factory, new File(LOG_DIRECTORY_NAME));
    }

    @Override
    public ManagedActor createActor(@NotNull final String name,
                                    @NotNull final String className,
                                    @NotNull final String jsonConfig) throws ValidationException {
        try {
            File tempConfigFile = File.createTempFile(TMP_CONFIG_FILE_PREFIX, TMP_CONFIG_FILE_SUFFIX);
            FileHelper.writeToFile(tempConfigFile, jsonConfig);

            Process process = forkProcess(name, className, tempConfigFile.getAbsolutePath());

            WatchDogThread thread = new WatchDogThread(name, process);
            thread.start();
            processes.put(name, thread);
        } catch (IOException e) {
            Throwables.propagate(e);
        }

        return new ManagedActor() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public void close() {
                WatchDogThread thread = processes.remove(name);
                if (thread == null) {
                    return;
                }

                terminateProcess(thread);
            }
        };
    }

    @VisibleForTesting
    Map<String, WatchDogThread> getProcesses() {
        return ImmutableMap.copyOf(processes);
    }

    @Override
    public void close() {
        for (WatchDogThread thread : processes.values()) {
            terminateProcess(thread);
        }
        processes.clear();
    }

    private void terminateProcess(final WatchDogThread thread) {
        thread.close();
        thread.getProcess().destroy();
        Uninterruptibles.joinUninterruptibly(thread);
    }

    private Process forkProcess(@NotNull final String name,
                                @NotNull final String className,
                                @NotNull final String configFileName) throws IOException {
        String[] cmd = { //
                StandardSystemProperty.JAVA_HOME.value() + JAVA_CMD_PATH, //
                "-cp", //
                StandardSystemProperty.JAVA_CLASS_PATH.value(), // Use the current classpath
                ActorBootstrap.class.getName(), // Main class
                name, // arg[0]
                className, // arg[1]
                configFileName // arg[2]
        };

        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.redirectErrorStream(true);

        String actorLogFileName = localLogDir.getAbsolutePath() + File.separator + name + ".log";
        builder.redirectOutput(new File(actorLogFileName));

        LOG.info("Started process with command " + builder.command() + ", logging to " + actorLogFileName);

        return builder.start();
    }

    static class WatchDogThread extends Thread {

        private static final Logger LOG = LoggerFactory.getLogger(WatchDogThread.class);

        private final String name;
        private final Process process;

        private volatile boolean doWork = true;
        private volatile boolean exited = false;

        private WatchDogThread(final String name, final Process process) {
            this.name = name;
            this.process = process;
            setName("WatchDog-" + name);
        }

        Process getProcess() {
            return process;
        }

        public void close() {
            doWork = false;
        }

        @Override
        public void run() {
            LOG.info(this + " Watching process " + process + "...");

            while (doWork && !exited) {
                try {
                    int exitCode = process.waitFor();
                    exited = true;
                    LOG.info(this + " Exited with code " + exitCode + ".");
                } catch (InterruptedException ignored) { // NOSONAR
                }
            }
        }

        @Override
        public String toString() {
            return "WatchDogThread{" +
                    '\'' + name + '\'' +
                    '}';
        }

        boolean hasExited() {
            return exited;
        }
    }

}
