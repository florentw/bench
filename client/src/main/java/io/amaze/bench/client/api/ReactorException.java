package io.amaze.bench.client.api;

import javax.validation.constraints.NotNull;

/**
 * A root type for errors to be thrown by an Actor while processing an incoming message.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 * @see IrrecoverableException
 * @see TerminationException
 */
public abstract class ReactorException extends Exception {

    ReactorException() {
        // To be overridden
    }

    ReactorException(@NotNull final String message) {
        super(message);
    }

    ReactorException(@NotNull final String message, @NotNull final Throwable cause) {
        super(message, cause);
    }
}
