package io.amaze.bench.client.runtime.agent;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/3/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class AgentInputMessage implements Serializable {

    private final String destinationAgent;
    private final Action action;
    private final Serializable data;

    public AgentInputMessage(@NotNull final String destinationAgent,
                             @NotNull final Action action,
                             @NotNull final Serializable data) {

        this.destinationAgent = checkNotNull(destinationAgent);
        this.action = checkNotNull(action);
        this.data = checkNotNull(data);
    }

    public Action getAction() {
        return action;
    }

    public Serializable getData() {
        return data;
    }

    public String getDestinationAgent() {
        return destinationAgent;
    }

    public enum Action {
        CREATE_ACTOR,
        CLOSE_ACTOR
    }
}
