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

import io.amaze.bench.shared.metric.Metric;
import io.amaze.bench.shared.metric.MetricsSink;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created on 2/29/16.
 */
public final class MetricsJsonHelperTest {

    private static final Metric METRIC_CPU = new Metric("Consumer CPU", "cycles", 100);

    private static final String EXPECTED_METRIC_JSON = "{\"label\":\"Consumer CPU\",\"firstUnit\":\"cycles\",\"value\":100}";

    private static final String EXPECTED_SINK_JSON = "{" + //
            "\"consumer.cpu\":{\"label\":\"Consumer CPU\",\"firstUnit\":\"cycles\",\"value\":100}," + //
            "\"consumer.mem.after\":{\"label\":\"Consumer Mem. After\",\"firstUnit\":\"bytes\",\"value\":2048}," + //
            "\"consumer.mem.before\":{\"label\":\"Consumer Mem. Before\",\"firstUnit\":\"bytes\",\"value\":1024}}";

    @Test
    public void serialize_metric() {
        assertThat(MetricsJsonHelper.toJsonObject(METRIC_CPU).toString(), is(EXPECTED_METRIC_JSON));
    }

    @Test
    public void serialize_metric_sink() {
        MetricsSink sink = MetricsSink.create();
        Metric memBefore = new Metric("Consumer Mem. Before", "bytes", 1024);
        Metric memAfter = new Metric("Consumer Mem. After", "bytes", 2048);
        sink.add("consumer.cpu", METRIC_CPU).add("consumer.mem.before", memBefore).add("consumer.mem.after", memAfter);

        Map<String, Metric> metrics = sink.getMetricsAndFlush();

        assertThat(MetricsJsonHelper.toJsonObject(metrics).toString(), is(EXPECTED_SINK_JSON));
    }

}