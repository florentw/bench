package io.amaze.bench.leader.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.leader.cluster.registry.MetricsRepositoryListener;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.runtime.actor.metric.MetricValuesMessage;
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
        doAnswer(invocation -> {
            jgroupsListener = (JgroupsListener<MetricValuesMessage>) invocation.getArguments()[1];
            return null;
        }).when(listenerMultiplexer).addListener(eq(MetricValuesMessage.class), any(JgroupsListener.class));
        MetricsRepositoryListener repositoryListener = mock(MetricsRepositoryListener.class);
        clusterClient.startMetricsListener(repositoryListener);
        MetricValuesMessage metricValues = new MetricValuesMessage(TestActor.DUMMY_ACTOR, new HashMap<>());

        jgroupsListener.onMessage(mock(Message.class), metricValues);

        verify(repositoryListener).onMetricValues(metricValues);
        verifyNoMoreInteractions(repositoryListener);
    }

    @Test
    public void close_removes_listener() {

        clusterClient.close();

        verify(listenerMultiplexer).removeListenerFor(eq(MetricValuesMessage.class));
        verifyNoMoreInteractions(listenerMultiplexer);
    }

}