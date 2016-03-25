package io.amaze.bench.client.runtime.agent;

import io.amaze.bench.client.runtime.actor.ActorFactory;
import io.amaze.bench.client.runtime.actor.ActorManager;
import io.amaze.bench.client.runtime.actor.EmbeddedActorManager;
import io.amaze.bench.client.runtime.orchestrator.JMSOrchestratorClientFactory;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;

/**
 * Created on 3/5/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class AgentBootstrap {

    public static void main(String[] args) throws Exception {

        // TODO INSTALL SHUTDOWN HOOK

        if (args.length != 2) {
            System.out.println("Usage:");
            System.out.println("$ benchclient <jmsServerHost> <jmsServerPort>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        OrchestratorClientFactory clientFactory = new JMSOrchestratorClientFactory(host, port);
        ActorManager manager = new EmbeddedActorManager(new ActorFactory(clientFactory));
        try (Agent agent = new Agent(clientFactory, manager)) {
            Thread.sleep(Long.MAX_VALUE);
        }
    }

}


