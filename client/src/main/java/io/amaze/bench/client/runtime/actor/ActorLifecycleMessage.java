package io.amaze.bench.client.runtime.actor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

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

    public ActorLifecycleMessage(@NotNull final String actor, @NotNull final Phase phase) {
        this(actor, phase, null);
    }

    public ActorLifecycleMessage(@NotNull final String actor, @NotNull final Phase phase, final Throwable throwable) {
        this.actor = actor;
        this.phase = phase;
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

    public enum Phase {
        CREATED,
        STARTED,
        FAILED,
        CLOSED
    }
}
