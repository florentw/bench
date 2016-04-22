package io.amaze.bench.orchestrator;

import io.amaze.bench.client.runtime.agent.AgentInputMessage;
import io.amaze.bench.orchestrator.registry.ActorRegistryListener;
import io.amaze.bench.orchestrator.registry.AgentRegistryListener;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created on 2/21/16
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface OrchestratorServer extends AutoCloseable {

    void startRegistryListeners(@NotNull AgentRegistryListener agentsListener,
                                @NotNull ActorRegistryListener actorsListener);

    void createActorQueue(@NotNull String actor);

    void deleteActorQueue(@NotNull String actor);

    void sendToActor(@NotNull String actorName, @NotNull Serializable message);

    void sendToAgent(@NotNull AgentInputMessage message);

    @Override
    void close();

}
