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
package io.amaze.bench.cluster.jms;

import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.actor.ActorInputMessage;
import io.amaze.bench.cluster.actor.ActorSender;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Throwables.propagate;
import static java.util.Objects.requireNonNull;

/**
 * Interface to send messages to actors within the system.
 */
public class JMSActorSender implements ActorSender {

    private final JMSClient client;

    public JMSActorSender(@NotNull final JMSClient client) {
        this.client = requireNonNull(client);
    }

    /**
     * Will send the given message to the specified actor using the underlying messaging system.
     *
     * @param key     The actor name to send the message to
     * @param message Contents of the message, {@link ActorInputMessage}
     */
    @Override
    public void send(@NotNull final ActorKey key, @NotNull final ActorInputMessage message) {
        requireNonNull(key);
        requireNonNull(message);

        try {
            client.sendToQueue(key.getName(), message);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }
}
