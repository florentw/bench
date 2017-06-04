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

import io.amaze.bench.cluster.metric.MetricValuesMessage;
import io.amaze.bench.cluster.metric.MetricsRepositoryListener;
import io.amaze.bench.shared.jms.JMSHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Created on 10/3/16.
 */
final class JMSMetricsRepositoryListener implements MessageListener {

    private static final Logger log = LogManager.getLogger();

    private final MetricsRepositoryListener metricsListener;

    JMSMetricsRepositoryListener(@NotNull final MetricsRepositoryListener metricsListener) {
        this.metricsListener = requireNonNull(metricsListener);
    }

    @Override
    public void onMessage(final Message jmsMessage) {
        requireNonNull(jmsMessage);

        Optional<MetricValuesMessage> metrics = readMessage(jmsMessage);
        if (!metrics.isPresent()) {
            return;
        }

        metricsListener.onMetricValues(metrics.get());
    }

    private Optional<MetricValuesMessage> readMessage(final Message jmsMessage) {
        try {
            return Optional.of(JMSHelper.objectFromMsg((BytesMessage) jmsMessage));
        } catch (Exception e) { // NOSONAR - We want to catch everything
            log.error("Error while reading JMS message.", e);
            return Optional.empty();
        }
    }
}
