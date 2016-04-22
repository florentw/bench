package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.api.actor.Reactor;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a {@link ActorValidator} instance to use for loading {@link Reactor} classes.
 * <p>
 * Created on 3/2/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 * @see ActorValidator
 */
final class ActorValidators {

    private static final ActorValidator impl = new ActorValidatorImpl();

    private ActorValidators() {
        // Factory
    }

    @NotNull
    public static ActorValidator get() {
        return impl;
    }

}
