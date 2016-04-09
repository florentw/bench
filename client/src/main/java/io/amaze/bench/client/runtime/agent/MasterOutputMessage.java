package io.amaze.bench.client.runtime.agent;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/3/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class MasterOutputMessage implements java.io.Serializable {

    private final Action action;
    private final Serializable data;

    public MasterOutputMessage(@NotNull final Action action, @NotNull final Serializable data) {
        this.action = checkNotNull(action);
        this.data = checkNotNull(data);
    }

    public Action getAction() {
        return action;
    }

    public Serializable getData() {
        return data;
    }

    public enum Action {
        REGISTER_AGENT,
        UNREGISTER_AGENT,
        ACTOR_LIFECYCLE
    }

}
