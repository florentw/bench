package io.amaze.bench.client.runtime.agent;

import io.amaze.bench.client.runtime.orchestrator.OrchestratorActor;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorAgent;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;

/**
 * Created on 3/30/16.
 */
public final class DummyClientFactory implements OrchestratorClientFactory {

    private final OrchestratorAgent agentClient;
    private final OrchestratorActor actorClient;

    public DummyClientFactory(final OrchestratorAgent agentClient, final OrchestratorActor actorClient) {
        this.agentClient = agentClient;
        this.actorClient = actorClient;
    }

    @Override
    public OrchestratorAgent createForAgent() {
        return agentClient;
    }

    @Override
    public OrchestratorActor createForActor() {
        return actorClient;
    }
}
