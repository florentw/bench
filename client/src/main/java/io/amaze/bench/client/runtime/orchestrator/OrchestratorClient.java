package io.amaze.bench.client.runtime.orchestrator;


import io.amaze.bench.client.runtime.actor.Actor;
import io.amaze.bench.client.runtime.agent.AgentClientListener;
import io.amaze.bench.client.runtime.message.Message;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created on 2/24/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface OrchestratorClient extends AutoCloseable {

    void startAgentListener(@NotNull final String agent,
                            @NotNull final String agentsTopic,
                            @NotNull final AgentClientListener listener);

    void startActorListener(@NotNull final Actor actor);

    void sendToActor(@NotNull final String to, @NotNull final Message<? extends Serializable> message);

}
