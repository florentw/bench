package io.amaze.bench.client.api;

import javax.validation.constraints.NotNull;

/**
 * To be thrown by an Actor when a non-recoverable error happens.<br/>
 * It will notify the agent of the failure, that can perform additional actions to help troubleshoot the issue.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class IrrecoverableException extends ReactorException {

    public IrrecoverableException(@NotNull final String message) {
        super(message);
    }

    public IrrecoverableException(@NotNull final String message, @NotNull final Throwable cause) {
        super(message, cause);
    }
}
