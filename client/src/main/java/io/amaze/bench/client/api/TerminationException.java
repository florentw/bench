package io.amaze.bench.client.api;

/**
 * To be thrown by an Actor to notify the agent that it wishes to terminate gracefully.<br/>
 * To notify of irrecoverable exceptions the agent, {@link IrrecoverableException} should be used instead.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class TerminationException extends ReactorException {

    public TerminationException() {
        super();
    }

    /**
     * Hidden constructor
     */
    TerminationException(final String message) {
        throw new UnsupportedOperationException();
    }

    /**
     * Hidden constructor
     */
    TerminationException(final String message, final Throwable cause) {
        throw new UnsupportedOperationException();
    }
}
