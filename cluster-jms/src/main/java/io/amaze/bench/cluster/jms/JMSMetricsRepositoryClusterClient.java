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
import io.amaze.bench.cluster.metric.MetricsRepositoryClusterClient;
import io.amaze.bench.cluster.metric.MetricsRepositoryListener;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSException;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.cluster.agent.Constants.METRICS_TOPIC;
import static java.util.Objects.requireNonNull;

/**
 * Created on 10/3/16.
 */
public final class JMSMetricsRepositoryClusterClient extends JMSClusterClient implements MetricsRepositoryClusterClient {

    public JMSMetricsRepositoryClusterClient(@NotNull final JMSEndpoint endpoint) {
        super(endpoint);
    }

    @VisibleForTesting
    JMSMetricsRepositoryClusterClient(@NotNull final JMSClient client) {
        super(client);
    }

    @Override
    public void startMetricsListener(@NotNull final MetricsRepositoryListener metricsListener) {
        requireNonNull(metricsListener);
        try {
            JMSMetricsRepositoryListener msgListener = new JMSMetricsRepositoryListener(metricsListener);
            getClient().addTopicListener(METRICS_TOPIC, msgListener);
            getClient().startListening();

        } catch (JMSException e) {
            throw propagate(e);
        }
    }

}
