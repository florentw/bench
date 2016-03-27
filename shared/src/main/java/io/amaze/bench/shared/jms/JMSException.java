package io.amaze.bench.shared.jms;

/**
 * Wrapper exception for all Exception occurring while interacting with the underlying JMS implementation.
 * <p>
 * Created on 3/27/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class JMSException extends Exception {

    public JMSException(final Throwable cause) {
        super(cause);
    }

}
