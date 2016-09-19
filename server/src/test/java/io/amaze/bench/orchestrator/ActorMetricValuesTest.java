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
package io.amaze.bench.orchestrator;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.client.runtime.actor.metric.MetricValue;
import io.amaze.bench.shared.test.Json;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.amaze.bench.api.metric.Metric.metric;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * Created on 9/18/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ActorMetricValuesTest {

    private static final Metric DUMMY_METRIC = metric("test", "meters").build();

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(ActorMetricValues.class);
        tester.testAllPublicInstanceMethods(metricValues(2));
    }

    @Test
    public void toString_yields_valid_json() {
        assertTrue(Json.isValid(metricValues(2).toString()));
        assertTrue(Json.isValid(emptyMetricValues().toString()));
    }

    @Test
    public void merge_metrics_with_new_metric_creates_it() {
        ActorMetricValues originalValues = emptyMetricValues();
        ActorMetricValues newValues = metricValues(3);

        originalValues.mergeWith(newValues);

        assertThat(originalValues.metrics().size(), is(2));
        assertThat(originalValues.metrics().get(DUMMY_METRIC).size(), is(3));
    }

    @Test
    public void merge_metrics_add_new_values_to_existing_ones() {
        ActorMetricValues originalValues = metricValues(2);
        ActorMetricValues newValues = metricValues(3);

        originalValues.mergeWith(newValues);

        assertThat(originalValues.metrics().size(), is(2));
        assertThat(originalValues.metrics().get(DUMMY_METRIC).size(), is(5));
    }

    @Test
    public void copy_return_new_instances() {
        Map<Metric, List<MetricValue>> metricValues = new HashMap<>();
        List<MetricValue> values = new ArrayList<>();
        values.add(new MetricValue(1));
        metricValues.put(DUMMY_METRIC, values);
        ActorMetricValues actorMetricValues = new ActorMetricValues(metricValues);

        assertNotSame(actorMetricValues, actorMetricValues.copy());
        assertNotSame(metricValues, actorMetricValues.metrics());
        assertThat(actorMetricValues.copy().metrics().size(), is(actorMetricValues.metrics().size()));
    }

    private ActorMetricValues emptyMetricValues() {
        return new ActorMetricValues(new HashMap<>());
    }

    private ActorMetricValues metricValues(int nbValues) {
        Map<Metric, List<MetricValue>> metricValues = new HashMap<>();
        List<MetricValue> values = new ArrayList<>();
        for (int i = 0; i < nbValues; i++) {
            values.add(new MetricValue(i));
        }
        metricValues.put(DUMMY_METRIC, values);
        metricValues.put(metric("test2", "seconds").build(), new ArrayList<>());

        return new ActorMetricValues(metricValues);
    }


}