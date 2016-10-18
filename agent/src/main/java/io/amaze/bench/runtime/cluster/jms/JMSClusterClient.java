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
package io.amaze.bench.runtime.cluster.jms;

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.runtime.actor.ActorInputMessage;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.cluster.ActorSender;
import io.amaze.bench.runtime.cluster.ClusterClient;
import io.amaze.bench.runtime.message.Message;
import io.amaze.bench.shared.jms.FFMQClient;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSException;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.runtime.agent.Constants.ACTOR_REGISTRY_TOPIC;

/**
 * Created on 3/3/16.
 */
abstract class JMSClusterClient implements ClusterClient, ActorSender {

    private final JMSClient client;
    private final JMSActorSender actorSender;

    @VisibleForTesting
    JMSClusterClient(@NotNull final JMSClient client) {
        this.client = checkNotNull(client);
        actorSender = initActorSender(client);
    }

    JMSClusterClient(@NotNull final JMSEndpoint endpoint) {
        checkNotNull(endpoint);

        try {
            client = new FFMQClient(endpoint);
        } catch (JMSException e) {
            throw propagate(e);
        }
        actorSender = initActorSender(client);
    }

    @Override
    public final void close() {
        client.close();
    }

    @Override
    public void sendToActor(@NotNull final ActorKey to, @NotNull final ActorInputMessage message) {
        checkNotNull(to);
        checkNotNull(message);

        actorSender.sendToActor(to, message);
    }

    final void sendToActorRegistry(final Message msg) {
        try {
            getClient().sendToTopic(ACTOR_REGISTRY_TOPIC, msg);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    final JMSClient getClient() {
        return client;
    }

    private JMSActorSender initActorSender(final JMSClient client) {
        return new JMSActorSender(client);
    }

}
