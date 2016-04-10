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

import static com.google.common.base.Preconditions.checkNotNull;
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

    private final Map<String, ProcessWatchDogThread> processes = new ConcurrentHashMap<>();
    private final File localLogDir;

    ForkedActorManager(@NotNull final String agent, @NotNull File localLogDir) {
        super(agent);
        this.localLogDir = checkNotNull(localLogDir);

        boolean success = localLogDir.mkdir();
        if (!success && !localLogDir.exists()) {
            throw new IllegalStateException("Could not create directory: " + localLogDir);
        }
    }

    ForkedActorManager(@NotNull final String agent) {
        this(agent, new File(LOG_DIRECTORY_NAME));
    }

    @Override
    public ManagedActor createActor(@NotNull final ActorConfig actorConfig) throws ValidationException {
        checkNotNull(actorConfig);

        final String actor = actorConfig.getName();
        Process process = createActorProcess(actorConfig, actor);

        ProcessWatchDogThread thread = new ProcessWatchDogThread(actor, process);
        thread.start();
        thread.awaitUntilStarted();

        processes.put(actor, thread);

        return new ManagedActor() {
            @Override
            public String name() {
                return actor;
            }

            @Override
            public void close() {
                ProcessWatchDogThread thread = processes.remove(actor);
                if (thread == null) {
                    return;
                }

                terminateProcess(thread);
            }
        };
    }

    private Process createActorProcess(final ActorConfig actorConfig, final String actor) {
        try {
            File tempConfigFile = File.createTempFile(TMP_CONFIG_FILE_PREFIX, TMP_CONFIG_FILE_SUFFIX);
            FileHelper.writeToFile(tempConfigFile, actorConfig.getActorJsonConfig());

            return forkProcess(actor,
                               actorConfig.getClassName(),
                               actorConfig.getDeployConfig().getJmsServerHost(),
                               actorConfig.getDeployConfig().getJmsServerPort(),
                               tempConfigFile.getAbsolutePath());

        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @VisibleForTesting
    Map<String, ProcessWatchDogThread> getProcesses() {
        return ImmutableMap.copyOf(processes);
    }

    @Override
    public void close() {
        for (ProcessWatchDogThread thread : processes.values()) {
            terminateProcess(thread);
        }
        processes.clear();
    }

    private void terminateProcess(final ProcessWatchDogThread thread) {
        thread.close();
        thread.getProcess().destroy();
        Uninterruptibles.joinUninterruptibly(thread);
    }

    private Process forkProcess(final String name,
                                final String className,
                                final String jmsServerHost,
                                final int jmsServerPort,
                                final String configFileName) throws IOException {
        String[] cmd = { //
                StandardSystemProperty.JAVA_HOME.value() + JAVA_CMD_PATH, //
                "-cp", //
                StandardSystemProperty.JAVA_CLASS_PATH.value(), // Use the current classpath
                ActorBootstrap.class.getName(),  // Main class
                getAgent(),                      // arg[0]
                name,                            // arg[1]
                className,                       // arg[2]
                jmsServerHost,                   // arg[3]
                Integer.toString(jmsServerPort), // arg[4]
                configFileName                   // arg[5]
        };

        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.redirectErrorStream(true);

        String actorLogFileName = localLogDir.getAbsolutePath() + File.separator + name + ".log";
        builder.redirectOutput(new File(actorLogFileName));

        LOG.info("Started process with command " + builder.command() + ", logging to " + actorLogFileName);

        return builder.start();
    }

}
