package io.amaze.bench.orchestrator.registry;

import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/28/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface AgentRegistryListener {

    void onAgentRegistration(@NotNull AgentRegistrationMessage msg);

    void onAgentSignOff(@NotNull String agent);

}
