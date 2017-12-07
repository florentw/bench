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

import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import io.amaze.bench.cluster.metric.MetricTimedValue;
import io.amaze.bench.cluster.metric.MetricValue;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Internal implementation of API interface {@link io.amaze.bench.api.metric.Metrics.Sink}
 *
 * @see MetricsInternal
 */
final class MetricSink implements Metrics.Sink {

    private final Collection<MetricValue> metricsValues;
    private final Map<Metric, List<MetricValue>> valuesMap;

    MetricSink(final Map<Metric, List<MetricValue>> valuesMap, final List<MetricValue> metricsValues) {
        this.valuesMap = requireNonNull(valuesMap);
        this.metricsValues = requireNonNull(metricsValues);
    }

    @Override
    public Metrics.Sink add(@NotNull final Number value) {
        synchronized (valuesMap) {
            metricsValues.add(new MetricValue(value));
        }
        return this;
    }

    @Override
    public Metrics.Sink timed(@NotNull final long timeStamp, @NotNull final Number value) {
        synchronized (valuesMap) {
            metricsValues.add(new MetricTimedValue(timeStamp, value));
        }
        return this;
    }

    @Override
    public Metrics.Sink timed(@NotNull final Number value) {
        return timed(System.currentTimeMillis(), value);
    }
}
