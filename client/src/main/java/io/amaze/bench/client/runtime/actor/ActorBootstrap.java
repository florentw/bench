package io.amaze.bench.client.runtime.actor;

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.client.runtime.orchestrator.JMSOrchestratorClientFactory;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;
import io.amaze.bench.shared.helper.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Acts as entry point for a forked actor JVM, created by a {@link ForkedActorManager}.
 * <p>
 * Created on 3/13/16.
 *
 * @see ForkedActorManager
 */
public final class ActorBootstrap {

    private static final Logger LOG = LoggerFactory.getLogger(ActorBootstrap.class);

    private final OrchestratorClientFactory clientFactory;
    private final String agentName;

    ActorBootstrap(@NotNull final String agentName, @NotNull final OrchestratorClientFactory clientFactory) {
        this.clientFactory = checkNotNull(clientFactory);
        this.agentName = checkNotNull(agentName);
    }

    /**
     * @param args [agentName] [actorName] [className] [jmsServerHost] [jmsServerPort] [temporaryConfigFile]
     * @throws ValidationException
     * @throws IOException
     */
    public static void main(final String[] args) throws ValidationException, IOException {
        checkNotNull(args);

        if (args.length != 6) {
            LOG.error("Usage:");
            LOG.error(
                    "ActorBootstrap <agentName> <actorName> <className> <jmsServerHost> <jmsServerPort> <temporaryConfigFile>");
            throw new IllegalArgumentException();
        }

        String agentName = args[0];
        String actorName = args[1];
        String className = args[2];
        String jmsServerHost = args[3];
        int jmsServerPort = Integer.parseInt(args[4]);
        String jsonTmpConfigFile = args[5];

        // Read and delete the temporary config file
        String jsonConfig = FileHelper.readFileAndDelete(jsonTmpConfigFile);

        OrchestratorClientFactory clientFactory = new JMSOrchestratorClientFactory(jmsServerHost, jmsServerPort);

        ActorBootstrap actorBootstrap = new ActorBootstrap(agentName, clientFactory);
        Actor actor = actorBootstrap.createActor(actorName, className, jsonConfig);

        installShutdownHook(actor);
    }

    @VisibleForTesting
    static Thread installShutdownHook(final Actor actor) {
        ActorShutdownThread hook = new ActorShutdownThread(actor);
        Runtime.getRuntime().addShutdownHook(hook);
        return hook;
    }

    Actor createActor(final String name, //
                      final String className, //
                      final String jsonConfig) throws ValidationException, IOException {

        ActorFactory actorFactory = new ActorFactory(agentName, clientFactory);
        return actorFactory.create(name, className, jsonConfig);
    }

    static final class ActorShutdownThread extends Thread {

        private final Actor actor;

        ActorShutdownThread(final Actor actor) {
            this.actor = actor;
        }

        @Override
        public void run() {
            LOG.info("ShutdownHook called for " + actor.name() + ".");
            actor.close();
        }
    }
}
