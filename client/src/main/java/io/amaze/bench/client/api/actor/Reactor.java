package io.amaze.bench.client.api.actor;


import io.amaze.bench.client.api.ReactorException;
import io.amaze.bench.client.api.TerminationException;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Provides a listener on messages sent by other reactors.<br/>
 * The listener must be parametrized the type of the message class it accepts (parameter [I]).<br/>
 * <p/>
 * [I] the message type should be a pure data bean, and needs to be {@link Serializable},
 * it can be sent over the wire for inter-process communications.
 * <p/>
 * Created on 2/28/16.
 *
 * @param <I> Input message type
 * @author Florent Weber (florent.weber@gmail.com)
 * @see io.amaze.bench.client.api.actor.Sender
 * @see Before
 * @see After
 */
public interface Reactor<I extends Serializable> {

    /**
     * The message dispatch/handling logic of an actor is to be implemented here.<br/>
     * This method will be called for each received message from other reactors.
     *
     * @param from    Source reactor name, unique in the cluster
     * @param message Received payload, a data bean
     * @throws ReactorException     Non-recoverable exceptions, will provoke a close of the actor and a call
     *                              to the "@{@link After}" method if any. The exception will then be propagated
     *                              for troubleshooting purpose.
     * @throws TerminationException An exception to be thrown by the Actor to signify that it needs to terminate
     *                              gracefully, it will provoke a close of the actor and a call
     *                              to the "@{@link After}" method if any.
     */
    void onMessage(@NotNull String from, @NotNull I message) throws ReactorException, TerminationException;

}
