package io.amaze.bench.client.runtime.agent;

import io.amaze.bench.client.runtime.orchestrator.OrchestratorClient;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;

/**
 * Created on 3/30/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class DummyClientFactory implements OrchestratorClientFactory {

    private final OrchestratorClient agentClient;
    private final OrchestratorClient actorClient;

    public DummyClientFactory(final OrchestratorClient agentClient, final OrchestratorClient actorClient) {
        this.agentClient = agentClient;
        this.actorClient = actorClient;
    }

    @Override
    public OrchestratorClient createForAgent() {
        return agentClient;
    }

    @Override
    public OrchestratorClient createForActor() {
        return actorClient;
    }
}
