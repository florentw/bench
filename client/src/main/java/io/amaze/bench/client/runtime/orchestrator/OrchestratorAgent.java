package io.amaze.bench.client.runtime.orchestrator;

import io.amaze.bench.client.runtime.agent.AgentClientListener;

import javax.validation.constraints.NotNull;

/**
 * Created on 4/24/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface OrchestratorAgent extends OrchestratorClient {

    /**
     * Starts a listener for the agent to listen to incoming messages.
     *
     * @param agent    The name of the agent that will be notified of messages addressed to him on the given listener.
     * @param listener An listener for the agent to be notified of incoming messages.
     */
    void startAgentListener(@NotNull final String agent, @NotNull final AgentClientListener listener);

}
