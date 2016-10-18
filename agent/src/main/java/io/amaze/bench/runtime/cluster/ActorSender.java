package io.amaze.bench.runtime.cluster;

import io.amaze.bench.runtime.actor.ActorInputMessage;
import io.amaze.bench.runtime.actor.ActorKey;

import javax.validation.constraints.NotNull;

/**
 * Created on 10/16/16.
 */
@FunctionalInterface
public interface ActorSender {

    /**
     * A call to this method will send a message {@code message} to the target actor {@code to}
     * using the underlying messaging system.
     *
     * @param to      Target actor key
     * @param message Payload to send
     */
    void send(@NotNull final ActorKey to, @NotNull final ActorInputMessage message);

}
