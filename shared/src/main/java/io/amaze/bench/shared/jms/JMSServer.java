package io.amaze.bench.shared.jms;

import org.jetbrains.annotations.NotNull;

/**
 * Abstraction facade for a JMS server implementation.<br/>
 * Implementations must start the underlying JMS server in the constructor and stop it in the {@link #close()} method.<br/>
 * Allows to manage the server (queues and topics management).
 * <p>
 * Created on 3/4/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface JMSServer extends AutoCloseable {

    /**
     * Create a non-persistent JMS queue via the underling server implementation.<br/>
     * Will throw if a queue with the same name already exist.
     *
     * @param name Name of the JMS queue to be created.
     * @throws JMSException
     */
    void createQueue(@NotNull String name) throws JMSException;

    /**
     * Create a non-persistent JMS topic via the underling server implementation.<br/>
     * Will throw if a queue with the same name already exist.
     *
     * @param name Name of the JMS topic to be created.
     * @throws JMSException
     */
    void createTopic(@NotNull String name) throws JMSException;

    /**
     * Deletes a previously created JMS queue via the underling server implementation.<br/>
     * If the queue does not exist, nothing happens.
     *
     * @param queue Name of the JMS queue to be deleted.
     */
    boolean deleteQueue(@NotNull String queue);

    /**
     * Deletes a previously created JMS topic via the underling server implementation.<br/>
     * If the topic does not exist, nothing happens.
     *
     * @param topic Name of the JMS topic to be deleted.
     */
    boolean deleteTopic(@NotNull String topic);

    @Override
    void close();

}
