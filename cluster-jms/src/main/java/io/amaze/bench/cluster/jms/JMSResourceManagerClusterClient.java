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

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.agent.AgentInputMessage;
import io.amaze.bench.cluster.leader.ResourceManagerClusterClient;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSServer;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.cluster.agent.Constants.AGENTS_TOPIC;
import static java.util.Objects.requireNonNull;

/**
 * Created on 3/8/16.
 */
public final class JMSResourceManagerClusterClient extends JMSClusterClient implements ResourceManagerClusterClient {

    private final JMSServer server;

    @VisibleForTesting
    JMSResourceManagerClusterClient(@NotNull final JMSServer server, @NotNull final JMSClient client) {
        super(client);
        this.server = requireNonNull(server);
    }

    JMSResourceManagerClusterClient(@NotNull final JMSServer server, @NotNull final JMSEndpoint endpoint) {
        super(endpoint);
        this.server = requireNonNull(server);
    }

    @Override
    public void initForActor(@NotNull final ActorKey actorKey) {
        requireNonNull(actorKey);

        try {
            server.createQueue(actorKey.getName());
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public void closeForActor(@NotNull final ActorKey actorKey) {
        requireNonNull(actorKey);

        server.deleteQueue(actorKey.getName());
    }

    @Override
    public void sendToAgent(@NotNull final AgentInputMessage message) {
        requireNonNull(message);

        try {
            getClient().sendToTopic(AGENTS_TOPIC, message);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }
}
