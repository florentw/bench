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
import io.amaze.bench.cluster.DummyEndpoint;
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

import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.cluster.agent.Constants.METRICS_TOPIC;
import static java.util.Objects.requireNonNull;

/**
 * Created on 4/24/16.
 */
final class JMSActorClusterClient extends JMSClusterClient implements ActorClusterClient {

    private final ActorKey actor;

    @VisibleForTesting
    JMSActorClusterClient(@NotNull final JMSClient client, @NotNull final ActorKey actor) {
        super(client);
        this.actor = requireNonNull(actor);
    }

    JMSActorClusterClient(@NotNull final JMSEndpoint endpoint, @NotNull final ActorKey actor) {
        super(endpoint);
        this.actor = requireNonNull(actor);
    }

    @Override
    public void startActorListener(@NotNull final RuntimeActor actor) {
        requireNonNull(actor);

        try {
            getClient().addQueueListener(actor.getKey().getName(), new JMSActorMessageListener(actor));
            getClient().startListening();
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public void sendMetrics(@NotNull final MetricValuesMessage metricValuesMessage) {
        requireNonNull(metricValuesMessage);

        try {
            getClient().sendToTopic(METRICS_TOPIC, metricValuesMessage);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    /**
     * @return A {@link DummyEndpoint} instance, as there is no way to contact an actor directly when using JMS.
     */
    @Override
    public Endpoint localEndpoint() {
        return new DummyEndpoint();
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
