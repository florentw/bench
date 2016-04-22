package io.amaze.bench.client.runtime.orchestrator;

import org.jetbrains.annotations.NotNull;

/**
 * Created on 3/1/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface OrchestratorClientFactory {

    @NotNull
    OrchestratorClient createForAgent();

    @NotNull
    OrchestratorClient createForActor();

}
