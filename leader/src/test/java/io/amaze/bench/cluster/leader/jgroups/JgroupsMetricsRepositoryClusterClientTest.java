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
package io.amaze.bench.cluster.leader.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.cluster.metric.MetricValuesMessage;
import io.amaze.bench.cluster.metric.MetricsRepositoryListener;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.shared.jgroups.JgroupsListener;
import io.amaze.bench.shared.jgroups.JgroupsListenerMultiplexer;
import org.jgroups.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 10/23/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsMetricsRepositoryClusterClientTest {

    @Mock
    private JgroupsListenerMultiplexer listenerMultiplexer;

    private JgroupsMetricsRepositoryClusterClient clusterClient;
    private JgroupsListener<MetricValuesMessage> jgroupsListener;

    @Before
    public void init() {
        clusterClient = new JgroupsMetricsRepositoryClusterClient(listenerMultiplexer);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(JgroupsMetricsRepositoryClusterClient.class);
        tester.testAllPublicInstanceMethods(clusterClient);
    }

    @Test
    public void startMetricsListener_registers_MetricValuesMessage_listener() {
        MetricsRepositoryListener repositoryListener = mock(MetricsRepositoryListener.class);

        clusterClient.startMetricsListener(repositoryListener);

        verify(listenerMultiplexer).addListener(eq(MetricValuesMessage.class), any(JgroupsListener.class));
        verifyNoMoreInteractions(listenerMultiplexer);
    }

    @Test
    public void jgroups_metrics_listener_forwards_messages() {
        spyOnListener();
        MetricsRepositoryListener repositoryListener = mock(MetricsRepositoryListener.class);
        clusterClient.startMetricsListener(repositoryListener);
        MetricValuesMessage metricValues = new MetricValuesMessage(TestActor.DUMMY_ACTOR, new HashMap<>());

        jgroupsListener.onMessage(mock(Message.class), metricValues);

        verify(repositoryListener).onMetricValues(metricValues);
        verifyNoMoreInteractions(repositoryListener);
    }

    @Test
    public void close_removes_listener() {
        spyOnListener();

        clusterClient.close();

        verify(listenerMultiplexer).removeListener(jgroupsListener);
        verifyNoMoreInteractions(listenerMultiplexer);
    }

    private void spyOnListener() {
        doAnswer(invocation -> {
            jgroupsListener = (JgroupsListener<MetricValuesMessage>) invocation.getArguments()[1];
            return null;
        }).when(listenerMultiplexer).addListener(eq(MetricValuesMessage.class), any(JgroupsListener.class));
    }

}