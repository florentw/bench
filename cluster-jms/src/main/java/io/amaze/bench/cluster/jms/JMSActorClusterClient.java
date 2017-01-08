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
import io.amaze.bench.cluster.Endpoint;
import io.amaze.bench.cluster.actor.ActorClusterClient;
import io.amaze.bench.cluster.actor.ActorRegistrySender;
import io.amaze.bench.cluster.actor.ActorSender;
import io.amaze.bench.cluster.actor.RuntimeActor;
import io.amaze.bench.cluster.metric.MetricValuesMessage;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSException;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.cluster.agent.Constants.METRICS_TOPIC;

/**
 * Created on 4/24/16.
 */
final class JMSActorClusterClient extends JMSClusterClient implements ActorClusterClient {

    private final ActorKey actor;

    @VisibleForTesting
    JMSActorClusterClient(@NotNull final JMSClient client, @NotNull final ActorKey actor) {
        super(client);
        this.actor = checkNotNull(actor);
    }

    JMSActorClusterClient(@NotNull final JMSEndpoint endpoint, @NotNull final ActorKey actor) {
        super(endpoint);
        this.actor = checkNotNull(actor);
    }

    @Override
    public void startActorListener(@NotNull final RuntimeActor actor) {
        checkNotNull(actor);

        try {
            getClient().addQueueListener(actor.getKey().getName(), new JMSActorMessageListener(actor));
            getClient().startListening();
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public void sendMetrics(@NotNull final MetricValuesMessage metricValuesMessage) {
        checkNotNull(metricValuesMessage);

        try {
            getClient().sendToTopic(METRICS_TOPIC, metricValuesMessage);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public Endpoint localEndpoint() {
        return new JMSEndpoint("dummy", 1337);
    }

    @Override
    public ActorSender actorSender() {
        return new JMSActorSender(getClient());
    }

    @Override
    public ActorRegistrySender actorRegistrySender() {
        return new JMSActorRegistrySender(getClient(), actor.getName());
    }
}
