package io.amaze.bench.client.runtime.orchestrator;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/1/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface OrchestratorClientFactory {

    @NotNull
    OrchestratorAgent createForAgent();

    @NotNull
    OrchestratorActor createForActor();

}
