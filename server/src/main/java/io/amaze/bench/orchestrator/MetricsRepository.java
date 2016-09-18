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
package io.amaze.bench.orchestrator;

import com.google.common.util.concurrent.SettableFuture;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.client.runtime.actor.metric.MetricValue;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSHelper;
import io.amaze.bench.shared.jms.JMSServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.client.runtime.agent.Constants.METRICS_ACTOR_NAME;

/**
 * Created on 9/17/16.
 */
public final class MetricsRepository {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsRepository.class);

    private final Map<String, ActorMetricValues> actorMetrics = new HashMap<>();
    private final Map<String, List<SettableFuture<ActorMetricValues>>> expectedActors = new HashMap<>();

    public MetricsRepository(@NotNull final JMSServer server, @NotNull final JMSClient client) {
        try {
            server.createQueue(METRICS_ACTOR_NAME);

            JMSMetricsRepositoryListener msgListener = new JMSMetricsRepositoryListener();
            client.addQueueListener(METRICS_ACTOR_NAME, msgListener);
            client.startListening();

        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    public ActorMetricValues metricsFor(@NotNull final String actor) {
        checkNotNull(actor);
        synchronized (actorMetrics) {
            return actorMetrics.get(actor).copy();
        }
    }

    public Future<ActorMetricValues> expectMetricsFor(@NotNull final String actor) {
        checkNotNull(actor);
        synchronized (actorMetrics) {
            Optional<Future<ActorMetricValues>> immediate = immediateResult(actor);
            if (immediate.isPresent()) {
                return immediate.get();
            }

            return registerExpectedActor(actor);
        }
    }

    public Map<String, ActorMetricValues> allMetrics() {
        synchronized (actorMetrics) {
            Map<String, ActorMetricValues> copy = new HashMap<>(actorMetrics.size());
            actorMetrics.forEach((actor, metricValues) -> copy.put(actor, actorMetrics.get(actor).copy()));
            return copy;
        }
    }

    private SettableFuture<ActorMetricValues> registerExpectedActor(final String actor) {
        List<SettableFuture<ActorMetricValues>> futures = expectedActors.get(actor);
        SettableFuture<ActorMetricValues> future = SettableFuture.create();
        if (futures != null) {
            futures.add(future);
        } else {
            List<SettableFuture<ActorMetricValues>> list = new ArrayList<>();
            list.add(future);
            expectedActors.put(actor, list);
        }
        return future;
    }

    private Optional<Future<ActorMetricValues>> immediateResult(final String actor) {
        ActorMetricValues metricValues = actorMetrics.get(actor);
        if (metricValues == null) {
            return Optional.empty();
        }

        SettableFuture<ActorMetricValues> future = SettableFuture.create();
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

            String actor = input.get().from();
            ActorMetricValues metrics = new ActorMetricValues((Map<Metric, List<MetricValue>>) input.get().data());

            updateMetricValues(actor, metrics);
        }

        private void updateMetricValues(final String actor, final ActorMetricValues metrics) {
            LOG.info("Received metrics from " + actor + " - " + metrics);

            synchronized (actorMetrics) {
                ActorMetricValues currentActorMetrics = updateActorMetricValues(actor, metrics);

                setExpectedActorFutures(actor, currentActorMetrics);
            }
        }

        private void setExpectedActorFutures(final String actor, final ActorMetricValues currentActorMetrics) {
            List<SettableFuture<ActorMetricValues>> futures = expectedActors.remove(actor);
            if (futures != null) {
                for (SettableFuture<ActorMetricValues> future : futures) {
                    future.set(currentActorMetrics);
                }
            }
        }

        private ActorMetricValues updateActorMetricValues(final String actor, final ActorMetricValues metrics) {
            ActorMetricValues currentActorMetrics = actorMetrics.remove(actor);
            if (currentActorMetrics != null) {
                currentActorMetrics.mergeWith(metrics);
            } else {
                currentActorMetrics = metrics;
            }
            actorMetrics.put(actor, currentActorMetrics);
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
