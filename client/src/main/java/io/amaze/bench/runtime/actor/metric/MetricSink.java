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
package io.amaze.bench.runtime.actor.metric;

import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Internal implementation of API interface {@link io.amaze.bench.api.metric.Metrics.Sink}
 *
 * @see MetricsInternal
 */
final class MetricSink implements Metrics.Sink {

    private final Collection<MetricValue> metricsValues;
    private final Map<Metric, List<MetricValue>> valuesList;

    MetricSink(final Map<Metric, List<MetricValue>> valuesList, final List<MetricValue> metricsValues) {
        this.valuesList = checkNotNull(valuesList);
        this.metricsValues = checkNotNull(metricsValues);
    }

    @Override
    public Metrics.Sink add(@NotNull final Number value) {
        synchronized (valuesList) {
            metricsValues.add(new MetricValue(value));
        }
        return this;
    }

    @Override
    public Metrics.Sink timed(@NotNull final long timeStamp, @NotNull final Number value) {
        synchronized (valuesList) {
            metricsValues.add(new MetricTimedValue(timeStamp, value));
        }
        return this;
    }

    @Override
    public Metrics.Sink timed(@NotNull final Number value) {
        synchronized (valuesList) {
            metricsValues.add(new MetricTimedValue(System.currentTimeMillis(), value));
        }
        return this;
    }
}
