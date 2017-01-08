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
package io.amaze.bench.cluster.metric;

import io.amaze.bench.api.ActorKey;
import io.amaze.bench.api.metric.Metric;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Immutable container for produced metric values for a given actor.
 * It contain a collection of {@link Metric} and their associated values lists.
 */
public final class MetricValuesMessage implements Serializable {

    private final ActorKey fromActor;
    private final Map<Metric, List<MetricValue>> metricValues; // NOSONAR: Serializable

    public MetricValuesMessage(@NotNull final ActorKey fromActor,
                               @NotNull final Map<Metric, List<MetricValue>> metricValues) {
        this.fromActor = checkNotNull(fromActor);
        this.metricValues = checkNotNull(metricValues);
    }

    /**
     * @return A deep copy of the metric to values map.
     */
    @NotNull
    public synchronized Map<Metric, List<MetricValue>> metrics() {
        Map<Metric, List<MetricValue>> copy = new HashMap<>(metricValues.size());
        metricValues.forEach((otherMetric, otherMetricValues) -> //
                                     copy.put(otherMetric, new ArrayList<>(otherMetricValues)));

        return copy;
    }

    /**
     * @return A deep copy of this{@link MetricValuesMessage}
     */
    @NotNull
    public synchronized MetricValuesMessage copy() {
        return new MetricValuesMessage(fromActor, metrics());
    }

    /**
     * @return The actor key that produced the metric values.
     */
    @NotNull
    public ActorKey fromActor() {
        return fromActor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromActor, metricValues);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetricValuesMessage that = (MetricValuesMessage) o;
        return Objects.equals(fromActor, that.fromActor) && Objects.equals(metricValues, that.metricValues);
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

    /**
     * Merges values from the given {@link MetricValuesMessage} to the current ones and returns a new object.
     * The state of the current instance is NOT changed.
     *
     * @param otherValues New metric values to be added to our current set.
     */
    @NotNull
    MetricValuesMessage mergeWith(@NotNull final MetricValuesMessage otherValues) {
        checkNotNull(otherValues);

        Map<Metric, List<MetricValue>> metricsCopy = metrics();

        otherValues.metrics().forEach((otherMetric, otherMetricValues) -> {
            List<MetricValue> existingValues = metricsCopy.get(otherMetric);
            if (existingValues != null) {
                existingValues.addAll(otherMetricValues);
            } else {
                metricsCopy.put(otherMetric, otherMetricValues);
            }
        });

        return new MetricValuesMessage(fromActor, metricsCopy);
    }
}
