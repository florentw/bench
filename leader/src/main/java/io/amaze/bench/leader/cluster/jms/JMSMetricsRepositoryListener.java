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

import io.amaze.bench.leader.cluster.registry.MetricsRepositoryListener;
import io.amaze.bench.runtime.actor.metric.MetricValuesMessage;
import io.amaze.bench.shared.jms.JMSHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/3/16.
 */
final class JMSMetricsRepositoryListener implements MessageListener {

    private static final Logger log = LogManager.getLogger();

    private final MetricsRepositoryListener metricsListener;

    JMSMetricsRepositoryListener(@NotNull final MetricsRepositoryListener metricsListener) {
        this.metricsListener = checkNotNull(metricsListener);
    }

    @Override
    public void onMessage(final Message jmsMessage) {
        checkNotNull(jmsMessage);

        Optional<MetricValuesMessage> metrics = readMessage(jmsMessage);
        if (!metrics.isPresent()) {
            return;
        }

        metricsListener.onMetricValues(metrics.get());
    }

    private Optional<MetricValuesMessage> readMessage(final Message jmsMessage) {
        try {
            return Optional.of(JMSHelper.objectFromMsg((BytesMessage) jmsMessage));
        } catch (Exception e) {
            log.error("Error while reading JMS message.", e);
            return Optional.empty();
        }
    }
}
