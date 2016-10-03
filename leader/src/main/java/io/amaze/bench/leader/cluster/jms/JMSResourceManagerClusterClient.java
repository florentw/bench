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
package io.amaze.bench.leader.cluster.jms;

import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.agent.AgentInputMessage;
import io.amaze.bench.leader.cluster.ResourceManagerClusterClient;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSServer;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.runtime.agent.Constants.AGENTS_TOPIC;

/**
 * Created on 3/8/16.
 */
public final class JMSResourceManagerClusterClient implements ResourceManagerClusterClient {

    private final JMSServer server;
    private final JMSClient client;

    public JMSResourceManagerClusterClient(@NotNull final JMSServer server, @NotNull final JMSClient client) {
        this.server = checkNotNull(server);
        this.client = checkNotNull(client);
    }

    @Override
    public void initForActor(@NotNull final ActorKey actorKey) {
        checkNotNull(actorKey);

        try {
            server.createQueue(actorKey.getName());
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public void closeForActor(@NotNull final ActorKey actorKey) {
        checkNotNull(actorKey);

        server.deleteQueue(actorKey.getName());
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
