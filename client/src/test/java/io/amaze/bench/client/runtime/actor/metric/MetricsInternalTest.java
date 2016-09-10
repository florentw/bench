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
package io.amaze.bench.client.runtime.actor.metric;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import io.amaze.bench.shared.test.Json;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static io.amaze.bench.api.metric.Metric.metric;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created on 4/17/16.
 */
public final class MetricsInternalTest {

    private static final Metric DUMMY_METRIC = metric("elapsed", "ms").build();

    private MetricsInternal metrics;

    @Before
    public void before() {
        metrics = MetricsInternal.create();
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(Metric.class, DUMMY_METRIC);
        tester.testAllPublicConstructors(Metrics.class);
        tester.testAllPublicInstanceMethods(metrics);
    }

    @Test
    public void call_to_add_adds_a_metric() {
        Metrics.Sink sink = this.metrics.sinkFor(DUMMY_METRIC);

        sink.add(10);

        Map<Metric, List<MetricValue>> values = metrics.dumpAndFlush();
        assertThat(values.size(), is(1));
        assertThat(values.get(DUMMY_METRIC).size(), is(1));
        assertThat(values.get(DUMMY_METRIC).get(0).getValue(), is(10));
    }

    @Test
    public void call_to_timed_adds_a_metric() {
        MetricSink sink = (MetricSink) this.metrics.sinkFor(DUMMY_METRIC);

        sink.timed(10);
        sink.timed(1337L, 11);

        Map<Metric, List<MetricValue>> values = metrics.dumpAndFlush();
        assertThat(values.size(), is(1));
        assertThat(values.get(DUMMY_METRIC).size(), is(2));
        assertThat(values.get(DUMMY_METRIC).get(0).getValue(), is(10));
        assertTrue(((MetricTimedValue) values.get(DUMMY_METRIC).get(0)).getTimestamp() > 0);
        assertThat(values.get(DUMMY_METRIC).get(1).getValue(), is(11));
        assertThat(((MetricTimedValue) values.get(DUMMY_METRIC).get(1)).getTimestamp(), is(1337L));
    }

    @Test
    public void adding_two_metrics_to_the_same_list_using_sinkFor() {
        metrics.sinkFor(DUMMY_METRIC).add(10);
        metrics.sinkFor(DUMMY_METRIC).add(11);

        Map<Metric, List<MetricValue>> values = metrics.dumpAndFlush();
        assertThat(values.size(), is(1));
        assertThat(values.get(DUMMY_METRIC).size(), is(2));
        assertThat(values.get(DUMMY_METRIC).get(0).getValue(), is(10));
        assertThat(values.get(DUMMY_METRIC).get(1).getValue(), is(11));
    }

    @Test
    public void call_to_get_and_flush_clears() {
        MetricSink sink = (MetricSink) this.metrics.sinkFor(DUMMY_METRIC);
        sink.add(10);

        // When
        metrics.dumpAndFlush();

        // Then
        assertTrue(metrics.dumpAndFlush().isEmpty());
    }

    @Test
    public void toString_yields_valid_json() {
        assertTrue(Json.isValid(DUMMY_METRIC.toString()));
    }
}