package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;

import javax.validation.constraints.NotNull;

/**
 * /**
 * Factory to create flavors of {@link ActorManager}:
 * <ul>
 * <li>{@link EmbeddedActorManager} a manager that will spawn instances of actor inside the current JVM.</li>
 * <li>{@link ForkedActorManager} will fork a new JVM to spawn each actor.</li>
 * </ul>
 * Created on 4/6/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 * @see ActorManager
 */
public interface ActorManagerFactory {

    /**
     * Will create a new instance of {@link ActorManager} that will instantiate actors in the current JVM.
     *
     * @param agentName The host agent name.
     * @param factory   An {@link OrchestratorClientFactory} to be used to create
     *                  {@link io.amaze.bench.client.runtime.orchestrator.OrchestratorActor} instances.
     * @return An instantiated {@link EmbeddedActorManager}
     */
    @NotNull
    ActorManager createEmbedded(@NotNull String agentName, @NotNull OrchestratorClientFactory factory);

    /**
     * Will create a new instance of {@link ActorManager} that will instantiate each actor in a new JVM.
     *
     * @param agentName The host agent name.
     * @return An instantiated {@link ForkedActorManager}
     */
    @NotNull
    ActorManager createForked(@NotNull String agentName);

}
