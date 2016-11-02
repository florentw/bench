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
package io.amaze.bench.cluster.metric;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.cluster.actor.ActorKey;
import io.amaze.bench.runtime.actor.TestActor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;
import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Created on 10/30/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class MetricsRepositoryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private MetricsRepository metricsRepository;

    @Before
    public void initMetricsRepository() {
        metricsRepository = new MetricsRepository();
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(MetricsRepository.class);
        tester.testAllPublicInstanceMethods(metricsRepository);
    }

    @Test
    public void metrics_are_added_on_message() throws IOException {
        MetricValuesMessage valuesMessage = metricsMessage(new ArrayList<>());
        MetricsRepositoryListener clusterListener = metricsRepository.createClusterListener();

        clusterListener.onMetricValues(valuesMessage);

        MetricValuesMessage metricValuesMessage = metricsRepository.valuesFor(DUMMY_ACTOR);
        assertThat(metricValuesMessage.metrics().size(), is(1));
    }

    @Test
    public void all_metrics_return_copy() throws IOException {
        MetricsRepositoryListener clusterListener = metricsRepository.createClusterListener();
        MetricValuesMessage valuesMessage = metricsMessage(new ArrayList<>());
        clusterListener.onMetricValues(valuesMessage);

        Map<ActorKey, MetricValuesMessage> allMetrics = metricsRepository.allValues();

        MetricValuesMessage metricValues = allMetrics.get(DUMMY_ACTOR);
        assertNotNull(metricValues);
        assertThat(metricValues.metrics().size(), is(1));
    }

    @Test
    public void metrics_are_merged_on_second_message() throws IOException {
        MetricsRepositoryListener clusterListener = metricsRepository.createClusterListener();
        MetricValuesMessage valuesMessage = metricsMessage(new ArrayList<>());
        List<MetricValue> values = new ArrayList<>();
        values.add(new MetricValue(1));
        MetricValuesMessage secondMessage = metricsMessage(values);

        clusterListener.onMetricValues(valuesMessage);
        clusterListener.onMetricValues(secondMessage);

        MetricValuesMessage metricValuesMessage = metricsRepository.valuesFor(DUMMY_ACTOR);
        assertThat(metricValuesMessage.metrics().size(), is(1));
        assertThat(metricValuesMessage.metrics().values().iterator().next().size(), is(1));
    }

    @Test
    public void expected_metrics_is_set_if_it_already_exists() throws IOException, ExecutionException {
        MetricsRepositoryListener clusterListener = metricsRepository.createClusterListener();
        MetricValuesMessage valuesMessage = metricsMessage(new ArrayList<>());
        clusterListener.onMetricValues(valuesMessage);

        Future<MetricValuesMessage> future = metricsRepository.expectValuesFor(DUMMY_ACTOR);

        assertThat(getUninterruptibly(future).metrics().size(), is(1));
    }

    @Test
    public void expected_metrics_futures_are_set_when_metrics_are_received() throws IOException, ExecutionException {
        MetricsRepositoryListener clusterListener = metricsRepository.createClusterListener();
        Future<MetricValuesMessage> firstFuture = metricsRepository.expectValuesFor(DUMMY_ACTOR);
        Future<MetricValuesMessage> secondFuture = metricsRepository.expectValuesFor(DUMMY_ACTOR);
        MetricValuesMessage valuesMessage = metricsMessage(new ArrayList<>());

        clusterListener.onMetricValues(valuesMessage);

        assertThat(getUninterruptibly(firstFuture).metrics().size(), is(1));
        assertThat(getUninterruptibly(secondFuture).metrics().size(), is(1));
    }

    private MetricValuesMessage metricsMessage(final List<MetricValue> values) throws IOException {
        Map<Metric, List<MetricValue>> metricValues = new HashMap<>();
        metricValues.put(Metric.metric("metric", "sec").build(), values);
        return new MetricValuesMessage(TestActor.DUMMY_ACTOR, metricValues);
    }

}