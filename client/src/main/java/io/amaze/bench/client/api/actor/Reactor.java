package io.amaze.bench.client.api.actor;


import io.amaze.bench.client.api.ReactorException;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Provides a listener on messages sent by other reactors.<br/>
 * The listener must be parametrized the type of the message class it accepts.<br/>
 * <p/>
 * Input message class should be a pure data bean.<br/>
 * <p/>
 * Created on 2/28/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 * @see io.amaze.bench.client.api.actor.Sender
 */
public interface Reactor<Input extends Serializable> {

    /**
     * The message dispatch/handling logic of an actor is to be implemented here.<br/>
     * This method will be called for each received message from other reactors.
     *
     * @param from    Source reactor name, unique in the cluster
     * @param message Received payload, a data bean
     * @throws ReactorException Non-recoverable exceptions
     */
    void onMessage(@NotNull String from, @NotNull Input message) throws ReactorException;

}
