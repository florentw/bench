package io.amaze.bench.client.runtime.agent;

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
     * @param actor      Actor name to createForAgent
     * @param className  Actor fully qualified class name
     * @param jsonConfig The actor configuration, arbitrary JSON format
     */
    void onActorCreationRequest(@NotNull String actor, @NotNull String className, @NotNull String jsonConfig);

    /**
     * Called when the agent is requested to close an actor.
     *
     * @param actor Actor to close
     */
    void onActorCloseRequest(@NotNull String actor);
}