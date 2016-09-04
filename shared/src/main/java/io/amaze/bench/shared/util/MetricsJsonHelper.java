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
package io.amaze.bench.shared.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.amaze.bench.shared.metric.Metric;

import javax.validation.constraints.NotNull;
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