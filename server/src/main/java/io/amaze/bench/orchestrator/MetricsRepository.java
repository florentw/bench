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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.client.runtime.agent.Constants.METRICS_ACTOR_NAME;

/**
 * Created on 9/17/16.
 */
public final class MetricsRepository {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsRepository.class);

    private final JMSClient client;

    public MetricsRepository(@NotNull final JMSServer server, @NotNull final JMSClient client) {
        this.client = checkNotNull(client);

        try {
            server.createQueue(METRICS_ACTOR_NAME);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    public void startListener() {
        try {
            JMSMetricsRepositoryListener msgListener = new JMSMetricsRepositoryListener();
            client.addQueueListener(METRICS_ACTOR_NAME, msgListener);
            client.startListening();

        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    private static final class JMSMetricsRepositoryListener implements MessageListener {

        @Override
        public void onMessage(final Message jmsMessage) {
            Optional<io.amaze.bench.client.runtime.message.Message> input = readMessage(jmsMessage);
            if (input.isPresent()) {
                String actor = input.get().from();
                Map<Metric, List<MetricValue>> metrics = (Map<Metric, List<MetricValue>>) input.get().data();
                LOG.info("Received metrics from " + actor + " - " + metrics);
            }
        }

        private Optional<io.amaze.bench.client.runtime.message.Message> readMessage(final javax.jms.Message jmsMessage) {
            try {
                return Optional.of(JMSHelper.objectFromMsg((BytesMessage) jmsMessage));
            } catch (Exception e) {
                LOG.error("Error while reading JMS message as Message.", e);
                return Optional.empty();
            }
        }
    }
}
