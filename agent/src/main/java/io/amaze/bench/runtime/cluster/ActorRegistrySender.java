package io.amaze.bench.runtime.cluster;

import io.amaze.bench.runtime.actor.ActorLifecycleMessage;

import javax.validation.constraints.NotNull;

/**
 * Created on 10/18/16.
 */
@FunctionalInterface
public interface ActorRegistrySender {

    /**
     * A call to this method will send a message {@code message} to the actor registry topic
     * using the underlying messaging system.
     *
     * @param actorLifecycleMessage Message to send to the topic
     */
    void send(@NotNull final ActorLifecycleMessage actorLifecycleMessage);

}
