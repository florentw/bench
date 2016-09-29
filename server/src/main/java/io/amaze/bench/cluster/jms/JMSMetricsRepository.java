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

import com.google.common.util.concurrent.SettableFuture;
import io.amaze.bench.client.runtime.actor.ActorKey;
import io.amaze.bench.client.runtime.actor.metric.MetricValuesMessage;
import io.amaze.bench.cluster.MetricsRepository;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.client.runtime.agent.Constants.METRICS_TOPIC;

/**
 * JMS implementation of the MetricsRepository (aggregate incoming metrics from actors).
 */
public final class JMSMetricsRepository implements MetricsRepository {

    private static final Logger LOG = LogManager.getLogger(JMSMetricsRepository.class);

    private final Map<ActorKey, MetricValuesMessage> actorValues = new HashMap<>();
    private final Map<ActorKey, List<SettableFuture<MetricValuesMessage>>> expectedActors = new HashMap<>();

    public JMSMetricsRepository(@NotNull final JMSClient client) {
        try {
            JMSMetricsRepositoryListener msgListener = new JMSMetricsRepositoryListener();
            client.addTopicListener(METRICS_TOPIC, msgListener);
            client.startListening();

        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public MetricValuesMessage valuesFor(@NotNull final ActorKey actor) {
        checkNotNull(actor);
        synchronized (actorValues) {
            return actorValues.get(actor).copy();
        }
    }

    @Override
    public Future<MetricValuesMessage> expectValuesFor(@NotNull final ActorKey actor) {
        checkNotNull(actor);
        synchronized (actorValues) {
            Optional<Future<MetricValuesMessage>> immediate = immediateResult(actor);
            if (immediate.isPresent()) {
                return immediate.get();
            }

            return registerExpectedActor(actor);
        }
    }

    @Override
    public Map<ActorKey, MetricValuesMessage> allValues() {
        synchronized (actorValues) {
            Map<ActorKey, MetricValuesMessage> copy = new HashMap<>(actorValues.size());
            actorValues.forEach((actor, metricValues) -> copy.put(actor, actorValues.get(actor).copy()));
            return copy;
        }
    }

    private SettableFuture<MetricValuesMessage> registerExpectedActor(final ActorKey actor) {
        List<SettableFuture<MetricValuesMessage>> futures = expectedActors.get(actor);
        SettableFuture<MetricValuesMessage> future = SettableFuture.create();
        if (futures != null) {
            futures.add(future);
        } else {
            List<SettableFuture<MetricValuesMessage>> list = new ArrayList<>();
            list.add(future);
            expectedActors.put(actor, list);
        }
        return future;
    }

    private Optional<Future<MetricValuesMessage>> immediateResult(final ActorKey actor) {
        MetricValuesMessage metricValues = actorValues.get(actor);
        if (metricValues == null) {
            return Optional.empty();
        }

        SettableFuture<MetricValuesMessage> future = SettableFuture.create();
        future.set(metricValues);
        return Optional.of(future);
    }

    private final class JMSMetricsRepositoryListener implements MessageListener {

        @Override
        public void onMessage(final Message jmsMessage) {
            Optional<io.amaze.bench.client.runtime.message.Message> input = readMessage(jmsMessage);
            if (!input.isPresent()) {
                return;
            }

            ActorKey actor = new ActorKey(input.get().from());
            MetricValuesMessage metrics = (MetricValuesMessage) input.get().data();

            updateMetricValues(actor, metrics);
        }

        private void updateMetricValues(final ActorKey actor, final MetricValuesMessage metrics) {
            LOG.info("Received metric values from {}: {}", actor, metrics);

            synchronized (actorValues) {
                MetricValuesMessage currentActorMetrics = updateActorMetricValues(actor, metrics);

                setExpectedActorFutures(actor, currentActorMetrics);
            }
        }

        private void setExpectedActorFutures(final ActorKey actor, final MetricValuesMessage currentActorMetrics) {
            List<SettableFuture<MetricValuesMessage>> futures = expectedActors.remove(actor);
            if (futures != null) {
                for (SettableFuture<MetricValuesMessage> future : futures) {
                    future.set(currentActorMetrics);
                }
            }
        }

        private MetricValuesMessage updateActorMetricValues(final ActorKey actor, final MetricValuesMessage metrics) {
            MetricValuesMessage currentActorMetrics = actorValues.remove(actor);
            if (currentActorMetrics != null) {
                currentActorMetrics.mergeWith(metrics);
            } else {
                currentActorMetrics = metrics;
            }
            actorValues.put(actor, currentActorMetrics);
            return currentActorMetrics;
        }

        private Optional<io.amaze.bench.client.runtime.message.Message> readMessage(final javax.jms.Message jmsMessage) {
            try {
                return Optional.of(JMSHelper.objectFromMsg((BytesMessage) jmsMessage));
            } catch (Exception e) {
                LOG.error("Error while reading JMS message.", e);
                return Optional.empty();
            }
        }
    }

}
