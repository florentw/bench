package io.amaze.bench.client.runtime.actor;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/13/16.
 */
abstract class AbstractActorManager implements ActorManager {

    private final String agent;

    AbstractActorManager(@NotNull final String agent) {
        this.agent = checkNotNull(agent);
    }

    protected final String getAgent() {
        return agent;
    }
}
