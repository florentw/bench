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
package io.amaze.bench.client.runtime.actor.metric;

import com.google.common.collect.ImmutableMap;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Internal implementation of {@link Metrics} interface provided to actors.
 */
public final class MetricsInternal implements Metrics {

    private final Map<Metric, List<MetricValue>> values = new HashMap<>();

    private MetricsInternal() {
        // Should not be public
    }

    public static MetricsInternal create() {
        return new MetricsInternal();
    }

    @Override
    public synchronized MetricSink sinkFor(final Metric metric) {
        checkNotNull(metric);

        if (!values.containsKey(metric)) {
            values.put(metric, new ArrayList<>());
        }
        return new MetricSink(values.get(metric));
    }

    public synchronized Map<Metric, List<MetricValue>> fetchAndFlush() {
        Map<Metric, List<MetricValue>> copy = ImmutableMap.copyOf(values);
        flush();
        return copy;
    }

    public synchronized void flush() {
        values.clear();
    }

}
