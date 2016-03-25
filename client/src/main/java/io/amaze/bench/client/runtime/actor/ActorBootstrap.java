package io.amaze.bench.client.runtime.actor;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.amaze.bench.client.runtime.orchestrator.JMSOrchestratorClientFactory;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;
import io.amaze.bench.shared.helper.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Acts as entry point for a forked actor JVM, created by a {@link ForkedActorManager}.
 * <p>
 * Created on 3/13/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 * @see ForkedActorManager
 */
public final class ActorBootstrap {

    private static final Logger LOG = LoggerFactory.getLogger(ActorBootstrap.class);

    private final OrchestratorClientFactory clientFactory;

    ActorBootstrap(final OrchestratorClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public static void main(String[] args) throws ValidationException, IOException {

        if (args.length != 3) {
            LOG.error("Usage:");
            LOG.error("ActorBootstrap <actorName> <className> <temporaryConfigFile>");
            throw new IllegalArgumentException();
        }

        String actorName = args[0];
        String className = args[1];
        String jsonTmpConfigFile = args[2];

        String jsonConfig = FileHelper.readFileAndDelete(jsonTmpConfigFile);

        OrchestratorClientFactory clientFactory = createClientFactory(jsonConfig);

        ActorBootstrap actorBootstrap = new ActorBootstrap(clientFactory);
        Actor actor = actorBootstrap.createActor(actorName, className, jsonConfig);

        installShutdownHook(actor);
    }

    @VisibleForTesting
    static Thread installShutdownHook(final Actor actor) {
        ActorShutdownThread hook = new ActorShutdownThread(actor);
        Runtime.getRuntime().addShutdownHook(hook);
        return hook;
    }

    private static OrchestratorClientFactory createClientFactory(final String jsonConfig) {
        Config config = ConfigFactory.parseString(jsonConfig);
        String host = config.getString("master.host");
        int port = config.getInt("master.port");
        return new JMSOrchestratorClientFactory(host, port);
    }

    Actor createActor(final String name,
                      final String className,
                      final String jsonConfig) throws ValidationException, IOException {

        ActorFactory actorFactory = new ActorFactory(clientFactory);

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
