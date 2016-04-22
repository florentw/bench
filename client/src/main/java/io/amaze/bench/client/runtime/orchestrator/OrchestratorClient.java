package io.amaze.bench.client.runtime.orchestrator;


import io.amaze.bench.client.runtime.actor.Actor;
import io.amaze.bench.client.runtime.agent.Agent;
import io.amaze.bench.client.runtime.agent.AgentClientListener;
import io.amaze.bench.client.runtime.message.Message;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Facade to interact with the underlying messaging system for an orchestration client (an {@link Agent}).<br/>
 * The alter-ego of this interface is OrchestratorServer on the orchestration server side.
 *
 * TODO: Split into 2 interfaces:
 * - OrchestratorClientAgent
 * - OrchestratorClientActor
 *
 * @author Florent Weber (florent.weber@gmail.com)
 * @see io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory
 * @see Agent
 */
public interface OrchestratorClient extends AutoCloseable {

    /**
     * Starts a listener for the agent to listen to incoming messages.
     *
     * @param agent    The name of the agent that will be notified of messages addressed to him on the given listener.
     * @param listener An listener for the agent to be notified of incoming messages.
     */
    void startAgentListener(@NotNull final String agent, @NotNull final AgentClientListener listener);

    /**
     * Starts a listener for the specified actor name to listen to incoming messages.
     *
     * @param actor The name of the agent that will be notified of messages addressed to it on the given listener.
     */
    void startActorListener(@NotNull final Actor actor);

    void sendToActor(@NotNull final String to, @NotNull final Message<? extends Serializable> message);

    @Override
    void close();
}
