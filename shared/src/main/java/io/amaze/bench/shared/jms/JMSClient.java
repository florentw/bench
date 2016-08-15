package io.amaze.bench.shared.jms;

import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created on 3/3/16.
 */
public interface JMSClient extends AutoCloseable {

    /**
     * Starts the previously registered queue and topic listeners.<br/>
     * The provided {@link javax.jms.MessageListener} instances will then be called upon reception off events.<br/>
     * This method need to be called only once, and once called, no other listeners can be added.<br/>
     *
     * @throws JMSException
     */
    void startListening() throws JMSException;

    /**
     * Registers a queue listener with the given {@link MessageListener}.<br/>
     * The listener object will then be notified of any new message once {@link #startListening()} is called.<br/>
     * Calls to this method after {@link #startListening()} was invoked will have no effect.
     *
     * @param listenerQueueName JMS queue to listen to
     * @param listener          Listener to be notified
     * @throws JMSException
     */
    void addQueueListener(@NotNull String listenerQueueName, @NotNull MessageListener listener) throws JMSException;

    /**
     * Registers a topic listener with the given {@link MessageListener}.<br/>
     * The listener object will then be notified of any new message once {@link #startListening()} is called.<br/>
     * Calls to this method after {@link #startListening()} was invoked will have no effect.
     *
     * @param listenerTopicName JMS topic to listen to
     * @param listener          Listener to be notified
     * @throws JMSException
     */
    void addTopicListener(@NotNull String listenerTopicName, @NotNull MessageListener listener) throws JMSException;

    /**
     * Sends a {@link Serializable} message to the specified JMS queue.
     *
     * @param queueName Destination JMS queue
     * @param msg       The message to send
     * @throws JMSException
     */
    void sendToQueue(@NotNull String queueName, @NotNull Serializable msg) throws JMSException;

    /**
     * Sends a {@link Serializable} message to the specified JMS topic.
     *
     * @param topicName Destination JMS topc
     * @param msg       The message to send
     * @throws JMSException
     */
    void sendToTopic(@NotNull String topicName, @NotNull Serializable msg) throws JMSException;

    @Override
    void close();

}
