package io.amaze.bench.shared.helper;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.amaze.bench.shared.metric.Metric;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;

final class MetricsJsonHelper {

    private static final GsonBuilder JSON_BUILDER = new GsonBuilder();

    private MetricsJsonHelper() {
        // Helper class
    }

    static JsonObject toJsonObject(@NotNull final Map<String, Metric> metrics) {
        TreeMap<String, Metric> sortedMetrics = new TreeMap<>(metrics);
        return JSON_BUILDER.create().toJsonTree(sortedMetrics).getAsJsonObject();
    }

    static JsonObject toJsonObject(@NotNull final Metric metric) {
        return JSON_BUILDER.create().toJsonTree(metric).getAsJsonObject();
    }
}