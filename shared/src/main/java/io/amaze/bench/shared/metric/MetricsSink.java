package io.amaze.bench.shared.metric;

import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created on 2/24/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class MetricsSink implements Serializable {

    private final Map<String, Metric> metrics = new TreeMap<>();

    private MetricsSink() {
        // Should not be instantiated
    }

    public static MetricsSink create() {
        return new MetricsSink();
    }

    public synchronized MetricsSink add(String key, Metric metric) {
        metrics.put(key, metric);
        return this;
    }

    public synchronized Map<String, Metric> metrics() {
        return ImmutableMap.copyOf(metrics);
    }
}
