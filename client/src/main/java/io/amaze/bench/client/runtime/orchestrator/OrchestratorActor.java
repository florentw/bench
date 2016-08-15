package io.amaze.bench.client.runtime.orchestrator;

import io.amaze.bench.client.runtime.actor.Actor;

import javax.validation.constraints.NotNull;

/**
 * Created on 4/24/16.
 */
public interface OrchestratorActor extends OrchestratorClient {

    /**
     * Starts a listener for the specified actor name to listen to incoming messages.
     *
     * @param actor The name of the agent that will be notified of messages addressed to it on the given listener.
     */
    void startActorListener(@NotNull final Actor actor);

}
