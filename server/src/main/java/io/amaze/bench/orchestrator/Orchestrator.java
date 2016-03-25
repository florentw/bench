package io.amaze.bench.orchestrator;

import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created on 2/21/16
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface Orchestrator extends AutoCloseable {

    void startListening();

    void onAgentRegistration(@NotNull AgentRegistrationMessage msg);

    void onAgentSignOff(@NotNull String agent);

    @NotNull
    List<AgentProxy> agents();

}
