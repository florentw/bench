package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Created on 4/6/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface ActorManagerFactory {

    @NotNull
    ActorManager createEmbedded(@NotNull String agentName, @NotNull OrchestratorClientFactory factory);

    @NotNull
    ActorManager createForked(@NotNull String agentName);

}
