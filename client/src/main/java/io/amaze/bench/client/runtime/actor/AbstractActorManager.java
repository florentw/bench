package io.amaze.bench.client.runtime.actor;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/13/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
abstract class AbstractActorManager implements ActorManager {

    private final ActorFactory factory;

    AbstractActorManager(@NotNull final ActorFactory factory) {
        this.factory = factory;
    }

    @NotNull
    final ActorFactory getFactory() {
        return factory;
    }
}
