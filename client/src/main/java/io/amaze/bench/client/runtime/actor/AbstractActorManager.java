package io.amaze.bench.client.runtime.actor;

import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/13/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
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
