package io.amaze.bench.client.api;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Interface to sendToQueue arbitrary messages to another {@link Reactor}.<br/>
 * <br/>
 * Classes annotated with @{@link Actor} MUST NOT implement it.<br/>
 * A Sender can be passed as parameter of an actor's constructor, it will then be automatically injected by the runtime.
 * <ul>
 * <li>No check is performed on the destination address (The destination reactor may not exist).</li>
 * <li>No delivery guarantee, the message is sent synchronously.</li>
 * </ul>
 * Created on 2/28/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 * @see Reactor
 */
public interface Sender {

    /**
     * Sends an arbitrary message to another {@link Reactor}.<br/>
     * No guarantee is made on the delivery. Sends the message asynchronously.
     *
     * @param to      Destination reactor's name, identifies uniquely a reactor in the cluster
     * @param message Payload to sendToQueue
     */
    void send(@NotNull final String to, @NotNull final Serializable message);

}
