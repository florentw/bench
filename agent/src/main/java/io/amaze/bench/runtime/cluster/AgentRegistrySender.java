package io.amaze.bench.runtime.cluster;

import io.amaze.bench.runtime.agent.AgentLifecycleMessage;

import javax.validation.constraints.NotNull;

/**
 * Created on 10/18/16.
 */
@FunctionalInterface
public interface AgentRegistrySender {

    /**
     * A call to this method will send a message {@code message} to the agent registry topic
     * using the underlying messaging system.
     *
     * @param message Lifecycle message to send
     */
    void send(@NotNull AgentLifecycleMessage message);

}
