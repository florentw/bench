/*
 * Copyright 2016 Florent Weber <florent.weber@gmail.com>
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

import javax.validation.constraints.NotNull;

/**
 * Abstraction facade for a JMS server implementation.<br/>
 * Implementations must start the underlying JMS server in the constructor and stop it in the {@link #close()} method.<br/>
 * Allows to manage the server (queues and topics management).
 * <p>
 * Created on 3/4/16.
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
