/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.amaze.bench.shared.jms;

import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Abstraction of a JMS client providing a way to listen for messages on queues, topics.
 * It also allows to send messages to a queue or a topic.<br>
 * A call to {@link #startListening()} can only be done once, and only after calling listener registration methods:<br>
 * <ul>
 * <li>{@link #addQueueListener(String, MessageListener)}</li>
 * <li>or {@link #addTopicListener(String, MessageListener)}</li>
 * </ul>
 */
public interface JMSClient extends AutoCloseable {

    /**
     * Starts the previously registered queue and topic listeners.<br>
     * The provided {@link javax.jms.MessageListener} instances will then be called upon reception off events.<br>
     * This method need to be called only once, and once called, no other listeners can be added.<br>
     *
     * @throws JMSException When an error happens while registering JMS listener
     */
    void startListening() throws JMSException;

    /**
     * Registers a queue listener with the given {@link MessageListener}.<br>
     * The listener object will then be notified of any new message once {@link #startListening()} is called.<br>
     * Calls to this method after {@link #startListening()} was invoked will have no effect.
     *
     * @param listenerQueueName JMS queue to listen to
     * @param listener          Listener to be notified
     * @throws JMSException When underlying JMS implementation fails
     */
    void addQueueListener(@NotNull String listenerQueueName, @NotNull MessageListener listener) throws JMSException;

    /**
     * Registers a topic listener with the given {@link MessageListener}.<br>
     * The listener object will then be notified of any new message once {@link #startListening()} is called.<br>
     * Calls to this method after {@link #startListening()} was invoked will have no effect.
     *
     * @param listenerTopicName JMS topic to listen to
     * @param listener          Listener to be notified
     * @throws JMSException When underlying JMS implementation fails
     */
    void addTopicListener(@NotNull String listenerTopicName, @NotNull MessageListener listener) throws JMSException;

    /**
     * Sends a {@link Serializable} message to the specified JMS queue.
     *
     * @param queueName Destination JMS queue
     * @param msg       The message to send
     * @throws JMSException When underlying JMS implementation fails
     */
    void sendToQueue(@NotNull String queueName, @NotNull Serializable msg) throws JMSException;

    /**
     * Sends a {@link Serializable} message to the specified JMS topic.
     *
     * @param topicName Destination JMS topc
     * @param msg       The message to send
     * @throws JMSException When underlying JMS implementation fails
     */
    void sendToTopic(@NotNull String topicName, @NotNull Serializable msg) throws JMSException;

    @Override
    void close();

}
