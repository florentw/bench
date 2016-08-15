package io.amaze.bench.client.api;

/**
 * To be thrown by an Actor to notify the agent that it wishes to terminate gracefully.<br/>
 * To notify of irrecoverable exceptions, {@link IrrecoverableException} should be used instead.
 */
public final class TerminationException extends ReactorException {

    public TerminationException() {
        super();
    }

}
