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
package io.amaze.bench.api.metric;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility to help create a new instance instance {@link Metric}.
 * <br>
 * Example:<br>
 * To express a metric that is a throughput in events/seconds you could have:
 * <pre> {@code
 * Metric.metric("logging.events", "events")
 *     .secondUnit("seconds")
 *     .label("Logging throughput")
 *     .minValue(0)
 *     .maxValue(1000)
 *     .build();
 * } </pre>
 */
public final class MetricBuilder {

    private final String key;
    private final String unit;
    private String label;
    private Number minValue;
    private Number maxValue;

    /**
     * @param key  Key of the metric, must be unique per {@link Metrics.Sink} and not empty or whitespace.
     * @param unit Name of the unit. (ex: km/s)
     */
    MetricBuilder(final String key, final String unit) {
        this.key = checkNotNull(key);
        this.unit = checkNotNull(unit);

        checkArgument(!key.trim().isEmpty(), "Key cannot be empty.");
    }

    /**
     * @param label An optional displayable name for this metric.
     * @return The builder instance
     */
    public MetricBuilder label(final String label) {
        this.label = checkNotNull(label);
        return this;
    }

    /**
     * @param minValue The optional minimum value that can be taken by values of this metric.
     * @return The builder instance
     */
    public MetricBuilder minValue(final Number minValue) {
        this.minValue = checkNotNull(minValue);
        return this;
    }

    /**
     * @param maxValue The optional maximum value that can be taken by values of this metric.
     * @return The builder instance
     */
    public MetricBuilder maxValue(final Number maxValue) {
        this.maxValue = checkNotNull(maxValue);
        return this;
    }

    /**
     * Builds the metric using previously set attributes.
     *
     * @return An instance of {@link Metric}.
     */
    public Metric build() {
        return new Metric(key, unit, label, minValue, maxValue);
    }
}