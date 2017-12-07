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
package io.amaze.bench.runtime.actor.metric;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import io.amaze.bench.cluster.metric.MetricTimedValue;
import io.amaze.bench.cluster.metric.MetricValuesMessage;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.shared.test.Json;
import org.junit.Before;
import org.junit.Test;

import static io.amaze.bench.api.metric.Metric.metric;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created on 4/17/16.
 */
public final class MetricsInternalTest {

    private static final Metric DUMMY_METRIC = metric("elapsed", "ms").build();
    private static final Metric ANOTHER_METRIC = metric("elapsed2", "ms").build();

    private MetricsInternal metrics;

    @Before
    public void before() {
        metrics = MetricsInternal.create(TestActor.DUMMY_ACTOR);
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
        Metrics.Sink sink = metrics.sinkFor(DUMMY_METRIC);

        sink.add(10);

        MetricValuesMessage values = metrics.dumpAndFlush();
        assertThat(values.fromActor(), is(TestActor.DUMMY_ACTOR));
        assertThat(values.metrics().size(), is(1));
        assertThat(values.metrics().get(DUMMY_METRIC).size(), is(1));
        assertThat(values.metrics().get(DUMMY_METRIC).get(0).getValue(), is(10));
    }

    @Test
    public void call_to_timed_adds_a_metric() {
        MetricSink sink = (MetricSink) metrics.sinkFor(DUMMY_METRIC);

        sink.timed(10);
        sink.timed(1337L, 11);

        MetricValuesMessage values = metrics.dumpAndFlush();
        assertThat(values.fromActor(), is(TestActor.DUMMY_ACTOR));
        assertThat(values.metrics().size(), is(1));
        assertThat(values.metrics().get(DUMMY_METRIC).size(), is(2));
        assertThat(values.metrics().get(DUMMY_METRIC).get(0).getValue(), is(10));
        assertTrue(((MetricTimedValue) values.metrics().get(DUMMY_METRIC).get(0)).getTimestamp() > 0);
        assertThat(values.metrics().get(DUMMY_METRIC).get(1).getValue(), is(11));
        assertThat(((MetricTimedValue) values.metrics().get(DUMMY_METRIC).get(1)).getTimestamp(), is(1337L));
    }

    @Test
    public void adding_two_metrics_to_the_same_list_using_sinkFor() {
        metrics.sinkFor(DUMMY_METRIC).add(10);
        metrics.sinkFor(DUMMY_METRIC).add(11);

        MetricValuesMessage values = metrics.dumpAndFlush();
        assertThat(values.fromActor(), is(TestActor.DUMMY_ACTOR));
        assertThat(values.metrics().size(), is(1));
        assertThat(values.metrics().get(DUMMY_METRIC).size(), is(2));
        assertThat(values.metrics().get(DUMMY_METRIC).get(0).getValue(), is(10));
        assertThat(values.metrics().get(DUMMY_METRIC).get(1).getValue(), is(11));
    }

    @Test
    public void call_to_get_and_flush_clears_lists() {
        MetricSink sink = (MetricSink) metrics.sinkFor(DUMMY_METRIC);
        sink.add(10);

        // When
        metrics.dumpAndFlush();

        // Then
        assertThat(metrics.getValues().size(), is(1));
        assertThat(metrics.getValues().get(DUMMY_METRIC).size(), is(0));
    }

    @Test
    public void call_to_flush_twice_does_not_remove_existing_lists() {
        MetricSink sink = (MetricSink) metrics.sinkFor(DUMMY_METRIC);
        sink.add(10);

        // When
        metrics.dumpAndFlush();

        MetricSink anotherSink = (MetricSink) metrics.sinkFor(ANOTHER_METRIC);
        anotherSink.add(11);

        MetricValuesMessage flushed = metrics.dumpAndFlush();

        // Then
        assertThat(metrics.getValues().size(), is(2));
        assertThat(metrics.getValues().get(DUMMY_METRIC).size(), is(0));
        assertThat(metrics.getValues().get(ANOTHER_METRIC).size(), is(0));
        assertThat(flushed.metrics().size(), is(1));
    }

    @Test
    public void toString_yields_valid_json() {
        assertTrue(Json.isValid(DUMMY_METRIC.toString()));
    }
}