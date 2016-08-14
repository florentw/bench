package io.amaze.bench.client.runtime.agent;

import io.amaze.bench.client.runtime.actor.ActorLifecycleMessage;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a message sent to the OrchestratorServer master from an {@link Agent}.<br/>
 * Created on 3/3/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 * @see Agent
 * @see ActorLifecycleMessage
 * @see AgentRegistrationMessage
 */
public final class MasterOutputMessage implements java.io.Serializable {

    private final Action action;
    private final Serializable data;

    public MasterOutputMessage(@NotNull final Action action, @NotNull final Serializable data) {
        this.action = checkNotNull(action);
        this.data = checkNotNull(data);
    }

    /**
     * @return An enum describing the data serialized along as payload.
     * @see Action for more information
     */
    public Action getAction() {
        return action;
    }

    /**
     * @return The serialized payload of which type is described by the {@link Action} enum carried.
     * @see Action for more information
     */
    public Serializable getData() {
        return data;
    }

    /**
     * Types of the object returned by {@link #getData()} when the value is:
     * <li>
     * <li>{@link #REGISTER_AGENT}: A {@link AgentRegistrationMessage} is returned
     * <li>{@link #UNREGISTER_AGENT}: A {@link String} is returned (the name of the agent signing off).</li>
     * <li>{@link #ACTOR_LIFECYCLE}: A {@link ActorLifecycleMessage}</li>
     * </ul>
     */
    public enum Action {
        REGISTER_AGENT, //
        UNREGISTER_AGENT, //
        ACTOR_LIFECYCLE //
    }

}
