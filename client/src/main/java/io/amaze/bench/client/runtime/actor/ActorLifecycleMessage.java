package io.amaze.bench.client.runtime.actor;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This message class represents actor lifecycle notifications.<br/>
 * It is meant to be sent by an agent to the master.<br/>
 * <p/>
 * Created on 3/6/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class ActorLifecycleMessage implements Serializable {

    private final String actor;
    private final Phase phase;
    private final Throwable throwable;
    private final String agent;

    public ActorLifecycleMessage(@NotNull final String actor, @NotNull final String agent, @NotNull final Phase phase) {
        this(actor, agent, phase, null);
    }

    public ActorLifecycleMessage(@NotNull final String actor,
                                 @NotNull final String agent,
                                 @NotNull final Phase phase,
                                 final Throwable throwable) {
        this.actor = checkNotNull(actor);
        this.agent = checkNotNull(agent);
        this.phase = checkNotNull(phase);
        this.throwable = throwable;
    }

    @NotNull
    public String getActor() {
        return actor;
    }

    @NotNull
    public Phase getPhase() {
        return phase;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public String getAgent() {
        return agent;
    }

    public enum Phase {
        CREATED,
        INITIALIZED,
        FAILED,
        CLOSED
    }
}
