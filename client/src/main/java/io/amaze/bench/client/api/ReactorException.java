package io.amaze.bench.client.api;

import org.jetbrains.annotations.NotNull;

/**
 * Created on 2/28/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class ReactorException extends Exception {

    public ReactorException(@NotNull final String message) {
        super(message);
    }

    public ReactorException(@NotNull final String message, final Throwable cause) {
        super(message, cause);
    }
}
