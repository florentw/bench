package io.amaze.bench.shared.metric;

import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 2/24/16.
 */
public final class MetricsSink implements Serializable {

    private final Map<String, Metric> metrics = new HashMap<>();

    private MetricsSink() {
        // Should not be public
    }

    public static MetricsSink create() {
        return new MetricsSink();
    }

    public synchronized MetricsSink add(@NotNull final String key, @NotNull final Metric metric) {
        checkNotNull(key);
        checkNotNull(metric);

        metrics.put(key, metric);
        return this;
    }

    public synchronized Map<String, Metric> getMetricsAndFlush() {
        Map<String, Metric> copy = ImmutableMap.copyOf(metrics);
        metrics.clear();
        return copy;
    }
}
