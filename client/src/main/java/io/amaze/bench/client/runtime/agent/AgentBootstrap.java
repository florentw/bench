package io.amaze.bench.client.runtime.agent;

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.client.runtime.actor.ActorManagers;
import io.amaze.bench.client.runtime.orchestrator.JMSOrchestratorClientFactory;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 3/5/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class AgentBootstrap {

    private static final Logger LOG = LoggerFactory.getLogger(AgentBootstrap.class);

    private AgentBootstrap() {
        // Should not be instantiated
    }

    public static void main(final String[] args) {
        if (args.length != 2) {
            LOG.info("Usage:");
            LOG.info("$ agent <jmsServerHost> <jmsServerPort>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        OrchestratorClientFactory clientFactory = new JMSOrchestratorClientFactory(host, port);

        Agent agent = createAgent(clientFactory);
        registerShutdownHook(agent);
    }

    @VisibleForTesting
    static Agent createAgent(final OrchestratorClientFactory clientFactory) {
        return new Agent(clientFactory, new ActorManagers());
    }

    @VisibleForTesting
    static Thread registerShutdownHook(final Agent agent) {
        AgentShutdownHook hook = new AgentShutdownHook(agent);
        Runtime.getRuntime().addShutdownHook(hook);
        return hook;
    }

    private static final class AgentShutdownHook extends Thread {
        private final Agent agent;

        AgentShutdownHook(final Agent agent) {
            this.agent = agent;
        }

        @Override
        public void run() {
            LOG.info("Calling shutdown hook for agent " + agent);
            try {
                agent.close();
            } catch (Exception e) {
                LOG.warn("Error while closing agent " + agent, e);
            }
        }
    }

}


