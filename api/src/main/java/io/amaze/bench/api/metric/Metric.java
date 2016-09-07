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
package io.amaze.bench.api.metric;


import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Serves as a definition for a type of metric.
 * Use {@link #metric(String, String)} method to create a new {@link Metric} instance.
 *
 * @see MetricBuilder
 */
public final class Metric implements Serializable {

    private final String key;
    private final String firstUnit;
    private final String secondUnit;
    private final String label;
    private final Number minValue;
    private final Number maxValue;

    Metric(@NotNull final String key,
           @NotNull final String firstUnit,
           final String label,
           final String secondUnit,
           final Number minValue,
           final Number maxValue) {

        this.key = checkNotNull(key);
        this.firstUnit = checkNotNull(firstUnit);
        this.label = label;
        this.secondUnit = secondUnit;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Creates a builder to generate a new {@link Metric}. The builder provides a fluent interface to declare it.
     *
     * @param key       Mandatory key of the metric, must be unique per {@link Metrics.Sink}.
     * @param firstUnit Mandatory name of the unit (ex: seconds).
     * @return A {@link MetricBuilder} instance.
     */
    public static MetricBuilder metric(final String key, final String firstUnit) {
        return new MetricBuilder(key, firstUnit);
    }

    /**
     * @return Key of the metric, must be unique per {@link Metrics.Sink}.
     */
    public String getKey() {
        return key;
    }

    /**
     * @return Label of the first (and possibly only unit). (ex: percent)
     */
    public String getFirstUnit() {
        return firstUnit;
    }

    /**
     * @return An {@link Optional} label for this metric.
     */
    public Optional<String> getLabel() {
        return Optional.ofNullable(label);
    }

    /**
     * @return Label of the {@link Optional} second unit. (as in meters per sec)
     */
    public Optional<String> getSecondUnit() {
        return Optional.ofNullable(secondUnit);
    }

    /**
     * @return A {@link Optional} minimum value that can be taken by values of this metric.
     */
    public Optional<Number> getMinValue() {
        return Optional.ofNullable(minValue);
    }

    /**
     * @return A {@link Optional} maximum value that can be taken by values of this metric.
     */
    public Optional<Number> getMaxValue() {
        return Optional.ofNullable(maxValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Metric metric = (Metric) o;
        return Objects.equals(key, metric.key);
    }

    @Override
    public String toString() {
        return "{\"key\":\"" + key + "\"" + ", " + //
                "\"label\":\"" + label + "\"" + ", " + //
                "\"firstUnit\":\"" + firstUnit + "\"" + ", " + //
                "\"secondUnit\":\"" + secondUnit + "\"" + ", " + //
                "\"minValue\":\"" + minValue + "\"" + ", " + //
                "\"maxValue\":\"" + maxValue + "\"" + "}";
    }
}