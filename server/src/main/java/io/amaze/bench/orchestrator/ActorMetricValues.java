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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 9/18/16.
 */
public final class ActorMetricValues {

    private final Map<Metric, List<MetricValue>> metricValues;

    ActorMetricValues(final Map<Metric, List<MetricValue>> metricValues) {
        this.metricValues = checkNotNull(metricValues);
    }

    public synchronized void mergeWith(ActorMetricValues otherValues) {
        checkNotNull(otherValues);

        otherValues.metrics().forEach((otherMetric, otherMetricValues) -> {
            List<MetricValue> existingValues = metricValues.get(otherMetric);
            if (existingValues != null) {
                existingValues.addAll(otherMetricValues);
            } else {
                metricValues.put(otherMetric, otherMetricValues);
            }
        });
    }

    public synchronized Map<Metric, List<MetricValue>> metrics() {
        Map<Metric, List<MetricValue>> copy = new HashMap<>(metricValues.size());
        metricValues.forEach((otherMetric, otherMetricValues) -> //
                                     copy.put(otherMetric, new ArrayList<>(otherMetricValues)));

        return copy;
    }

    public synchronized ActorMetricValues copy() {
        return new ActorMetricValues(metrics());
    }

    @Override
    public synchronized String toString() {
        StringBuilder out = new StringBuilder("{\"metricValues\":{");
        AtomicInteger line = new AtomicInteger(metricValues.size());
        for (Map.Entry<Metric, List<MetricValue>> entry : metricValues.entrySet()) {
            out.append("\"metric\":").append(entry.getKey());
            out.append(", \"values\":").append(Arrays.toString(entry.getValue().toArray()));
            if (line.decrementAndGet() != 0) {
                out.append(",");
            }
        }
        return out.append("}}").toString();
    }
}
