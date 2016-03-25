package io.amaze.bench.shared.helper;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.amaze.bench.shared.metric.Metric;
import io.amaze.bench.shared.metric.MetricsSink;

import javax.validation.constraints.NotNull;

final class MetricsJsonHelper {

    private static final GsonBuilder JSON_BUILDER = new GsonBuilder();

    private MetricsJsonHelper() {
        // Helper class
    }

    static JsonObject toJsonObject(@NotNull final MetricsSink metrics) {
        return JSON_BUILDER.create().toJsonTree(metrics.metrics()).getAsJsonObject();
    }

    static JsonObject toJsonObject(@NotNull final Metric metricCpu) {
        return JSON_BUILDER.create().toJsonTree(metricCpu).getAsJsonObject();
    }
}