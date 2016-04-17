package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.api.actor.After;
import io.amaze.bench.client.api.actor.Before;
import io.amaze.bench.client.api.actor.Reactor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Wrapper interface for a {@link Reactor} instance.<br/>
 * Used by the Manager as an internal interface with an embedded {@link Reactor} instance.<br/>
 * <p/>
 * Created on 2/28/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface Actor extends AutoCloseable {

    /**
     * The actor's name must be unique across the cluster.
     * It's the instance unique key.
     *
     * @return The actor's name.
     */
    String name();

    /**
     * Initializes the actor by invoking the method annotated with @{@link Before} if any.<br/>
     * Will send failure messages to the master.<br/>
     * Send "initialized" lifecycle notification if successful.<br/>
     */
    void init();

    /**
     * Dump the actor's accumulated getMetricsAndFlush and sends a message to the getMetricsAndFlush actor for collection.<br/>
     * Will send failure messages to the master.<br/>
     */
    void dumpAndFlushMetrics();

    /**
     * Hook for the actor to receive messages.<br/>
     * Will send failure messages to the master.<br/>
     *
     * @param from    Source actor
     * @param message Payload
     */
    void onMessage(@NotNull String from, @NotNull Serializable message);

    /**
     * Closes the actor and invokes the {@link Reactor} method annotated with @{@link After}.<br/>
     * Will send failure messages to the master.<br/>
     * Send "closed" lifecycle notification if successful.<br/>
     */
    @Override
    void close();

}
