package io.amaze.bench.client.runtime.actor;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/13/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
abstract class AbstractActorManager implements ActorManager {

    private final ActorFactory factory;
    private final String agent;

    AbstractActorManager(@NotNull final String agent, @NotNull final ActorFactory factory) {
        this.agent = agent;
        this.factory = factory;
    }

    protected final String getAgent() {
        return agent;
    }

    @NotNull
    final ActorFactory getFactory() {
        return factory;
    }
}
