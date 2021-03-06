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
package io.amaze.bench.runtime.actor.metric;

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.api.ActorKey;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import io.amaze.bench.cluster.metric.MetricValue;
import io.amaze.bench.cluster.metric.MetricValuesMessage;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Internal implementation of {@link Metrics} interface provided to actors.
 *
 * @see MetricSink
 */
public final class MetricsInternal implements Metrics {

    private final Map<Metric, List<MetricValue>> values = new HashMap<>();
    private final ActorKey actor;

    private MetricsInternal(@NotNull final ActorKey actor) {
        this.actor = requireNonNull(actor);
    }

    public static MetricsInternal create(@NotNull final ActorKey actorKey) {
        return new MetricsInternal(actorKey);
    }

    @Override
    public Sink sinkFor(@NotNull final Metric metric) {
        requireNonNull(metric);
        synchronized (values) {
            List<MetricValue> metricsValues = values.computeIfAbsent(metric, k -> new ArrayList<>());
            return new MetricSink(values, metricsValues);
        }
    }

    public MetricValuesMessage dumpAndFlush() {
        Map<Metric, List<MetricValue>> copy = new HashMap<>();
        synchronized (values) {
            values.forEach((metric, metricValues) -> {
                if (!metricValues.isEmpty()) {
                    copy.put(metric, new ArrayList<>(metricValues));
                    metricValues.clear();
                }
            });
        }
        return new MetricValuesMessage(actor, copy);
    }

    @VisibleForTesting
    Map<Metric, List<MetricValue>> getValues() {
        return values;
    }
}
