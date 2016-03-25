package io.amaze.bench.shared.jms;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.naming.NamingException;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created on 3/3/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
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

    void addQueueListener(@NotNull String listenerQueueName,
                          @NotNull MessageListener listener) throws NamingException, JMSException;

    void addTopicListener(@NotNull String listenerTopicName,
                          @NotNull MessageListener listener) throws NamingException, JMSException;

    void sendToQueue(@NotNull String queueName,
                     @NotNull Serializable msg) throws NamingException, JMSException, InterruptedException, IOException, ClassNotFoundException;

    void sendToTopic(@NotNull String topicName,
                     @NotNull Serializable msg) throws NamingException, JMSException, InterruptedException, IOException, ClassNotFoundException;
}
