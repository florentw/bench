package io.amaze.bench.orchestrator.registry;

import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/29/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class RegisteredActor {

    private final String actor;
    private final String agent;
    private final State state;

    RegisteredActor(@NotNull final String actor, @NotNull final String agent, @NotNull final State state) {

        this.actor = checkNotNull(actor);
        this.agent = checkNotNull(agent);
        this.state = checkNotNull(state);
    }

    public String getName() {
        return actor;
    }

    public State getState() {
        return state;
    }

    public String getAgent() {
        return agent;
    }

    public enum State {
        CREATED,
        INITIALIZED
    }
}
