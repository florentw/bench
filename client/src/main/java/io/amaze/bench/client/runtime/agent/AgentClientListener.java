package io.amaze.bench.client.runtime.agent;

import io.amaze.bench.client.runtime.actor.ActorConfig;

import javax.validation.constraints.NotNull;

/**
 * Interface to be implemented by an agent. These hooks are called upon reception of messages from the master.
 * All these methods' implementations should fail silently.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface AgentClientListener {

    /**
     * Method is called when an actor creation request is received from the master.
     *
     * @param actorConfig The actor configuration
     */
    void onActorCreationRequest(@NotNull ActorConfig actorConfig);

    /**
     * Called when the agent is requested to close an actor.
     *
     * @param actor Actor to close
     */
    void onActorCloseRequest(@NotNull String actor);
}