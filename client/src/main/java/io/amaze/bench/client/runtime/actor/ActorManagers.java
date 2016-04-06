package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;

import javax.validation.constraints.NotNull;

/**
 * Created on 4/6/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class ActorManagers implements ActorManagerFactory {

    @NotNull
    @Override
    public ActorManager createEmbedded(@NotNull final String agentName,
                                       @NotNull final OrchestratorClientFactory factory) {
        return new EmbeddedActorManager(agentName, new ActorFactory(agentName, factory));
    }

    @NotNull
    @Override
    public ActorManager createForked(@NotNull final String agentName) {
        return new ForkedActorManager(agentName);
    }

}
