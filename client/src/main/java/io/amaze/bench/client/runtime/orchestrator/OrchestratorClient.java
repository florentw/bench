package io.amaze.bench.client.runtime.orchestrator;


import io.amaze.bench.client.runtime.agent.Agent;
import io.amaze.bench.client.runtime.message.Message;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Facade to interact with the underlying messaging system for an orchestration client (an {@link Agent}).<br/>
 * The alter-ego of this interface is OrchestratorServer on the orchestration server side.
 * <p>
 *
 * @author Florent Weber (florent.weber@gmail.com)
 * @see io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory
 * @see Agent
 * @see io.amaze.bench.client.runtime.actor.BaseActor
 */
public interface OrchestratorClient extends AutoCloseable {

    /**
     * A call to this method will send a message {@code message} to the target actor {@code to}
     * using the underlying messaging system.
     *
     * @param to      Target actor name
     * @param message Payload to send
     */
    void sendToActor(@NotNull final String to, @NotNull final Message<? extends Serializable> message);

    /**
     * Will release resource on the underlying messaging system
     */
    @Override
    void close();
}
