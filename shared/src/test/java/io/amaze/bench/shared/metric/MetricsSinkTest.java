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
package io.amaze.bench.shared.metric;

import com.google.common.testing.NullPointerTester;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created on 4/17/16.
 */
public final class MetricsSinkTest {

    private static final Metric DUMMY_METRIC = new Metric("", "", 0);
    private MetricsSink sink;

    @Before
    public void before() {
        sink = MetricsSink.create();
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(Metric.class, DUMMY_METRIC);
        tester.testAllPublicConstructors(MetricsSink.class);
        tester.testAllPublicInstanceMethods(sink);
    }

    @Test
    public void call_to_add_adds_a_metric() {
        // When
        sink.add("test", DUMMY_METRIC);

        Map<String, Metric> metrics = sink.getMetricsAndFlush();
        assertThat(metrics.size(), is(1));
        assertThat(metrics.get("test"), is(DUMMY_METRIC));
    }

    @Test
    public void call_to_get_and_flush_clears() {
        sink.add("test", DUMMY_METRIC);

        // When
        sink.getMetricsAndFlush();

        // Then
        assertTrue(sink.getMetricsAndFlush().isEmpty());
    }
}