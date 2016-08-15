package io.amaze.bench.client.runtime.orchestrator;

import javax.validation.constraints.NotNull;

/**
 * Factory for interfaces to interact with orchestration layer.
 * <ul>
 * <li>Provides {@link OrchestratorAgent} instances for the {@link io.amaze.bench.client.runtime.agent.Agent}
 * to listen to incoming messages.</li>
 * <li>Provides {@link OrchestratorActor} instances for actors to listen to incoming messages
 * and be able to send back.</li>
 * </ul>
 *
 * @see OrchestratorAgent
 * @see OrchestratorActor
 */
public interface OrchestratorClientFactory {

    @NotNull
    OrchestratorAgent createForAgent();

    @NotNull
    OrchestratorActor createForActor();

}
