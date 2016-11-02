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
package io.amaze.bench.cluster.jgroups;

import io.amaze.bench.cluster.metric.MetricValuesMessage;
import io.amaze.bench.cluster.metric.MetricsRepositoryClusterClient;
import io.amaze.bench.cluster.metric.MetricsRepositoryListener;
import io.amaze.bench.shared.jgroups.JgroupsListener;
import io.amaze.bench.shared.jgroups.JgroupsListenerMultiplexer;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/23/16.
 */
public final class JgroupsMetricsRepositoryClusterClient implements MetricsRepositoryClusterClient {

    private final JgroupsListenerMultiplexer multiplexer;
    private JgroupsListener<MetricValuesMessage> jgroupsListener;

    public JgroupsMetricsRepositoryClusterClient(@NotNull final JgroupsListenerMultiplexer multiplexer) {
        this.multiplexer = checkNotNull(multiplexer);
    }

    @Override
    public void startMetricsListener(@NotNull final MetricsRepositoryListener metricsListener) {
        checkNotNull(metricsListener);

        jgroupsListener = (msg, payload) -> {
            checkNotNull(msg);
            checkNotNull(payload);

            metricsListener.onMetricValues(payload);
        };
        multiplexer.addListener(MetricValuesMessage.class, jgroupsListener);
    }

    @Override
    public void close() {
        multiplexer.removeListener(jgroupsListener);
    }

}
