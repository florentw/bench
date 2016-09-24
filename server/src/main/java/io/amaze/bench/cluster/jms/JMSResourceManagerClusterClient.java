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
package io.amaze.bench.cluster.jms;

import io.amaze.bench.client.runtime.agent.AgentInputMessage;
import io.amaze.bench.cluster.ResourceManagerClusterClient;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSServer;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.client.runtime.agent.Constants.AGENTS_TOPIC;

/**
 * Created on 3/8/16.
 */
public final class JMSResourceManagerClusterClient implements ResourceManagerClusterClient {

    private final JMSServer server;
    private final JMSClient client;

    public JMSResourceManagerClusterClient(@NotNull final JMSServer server, @NotNull final JMSClient client) {
        this.server = checkNotNull(server);
        this.client = checkNotNull(client);

        try {
            // This topic is used by the resource manager to send messages to agents
            server.createTopic(AGENTS_TOPIC);

        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public void initForActor(@NotNull final String actorName) {
        checkNotNull(actorName);

        try {
            server.createQueue(actorName);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public void closeForActor(@NotNull final String actorName) {
        checkNotNull(actorName);

        server.deleteQueue(actorName);
    }

    @Override
    public void sendToAgent(@NotNull final AgentInputMessage message) {
        checkNotNull(message);

        try {
            client.sendToTopic(AGENTS_TOPIC, message);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }
}
