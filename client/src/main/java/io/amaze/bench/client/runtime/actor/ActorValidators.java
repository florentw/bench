package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.api.Reactor;

import javax.validation.constraints.NotNull;

/**
 * Provides a {@link ActorValidator} instance to use for loading {@link Reactor} classes.
 * <p>
 * Created on 3/2/16.
 *
 * @see ActorValidator
 */
final class ActorValidators {

    private static final ActorValidator impl = new ActorValidatorImpl();

    private ActorValidators() {
        // Factory
    }

    /**
     * @return An Actor validator instance.
     */
    @NotNull
    public static ActorValidator get() {
        return impl;
    }

}
