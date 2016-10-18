package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.actor.ActorInputMessage;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.actor.RuntimeActor;
import io.amaze.bench.runtime.actor.metric.MetricValuesMessage;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.shared.jgroups.JgroupsListenerMultiplexer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.runtime.cluster.jgroups.JgroupsActorClusterClient.MessageListener;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 10/14/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsActorClusterClientTest {

    @Mock
    private JgroupsListenerMultiplexer listenerMultiplexer;
    @Mock
    private JgroupsSender jgroupsSender;
    @Mock
    private Endpoint endpoint;
    @Mock
    private RuntimeActor runtimeActor;
    @Mock
    private ActorRegistry actorRegistry;

    private JgroupsActorClusterClient clusterClient;

    @Before
    public void init() {
        clusterClient = new JgroupsActorClusterClient(endpoint, listenerMultiplexer, jgroupsSender, actorRegistry);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorInputMessage.class, ActorInputMessage.init());
        tester.setDefault(ActorKey.class, DUMMY_ACTOR);
        tester.setDefault(JgroupsSender.class, jgroupsSender);

        tester.testAllPublicConstructors(JgroupsActorClusterClient.class);
        tester.testAllPublicInstanceMethods(clusterClient);
    }

    @Test
    public void starting_actor_listener_registers_it() {

        clusterClient.startActorListener(runtimeActor);

        verify(listenerMultiplexer).addListener(eq(ActorInputMessage.class), any(MessageListener.class));
        verifyNoMoreInteractions(listenerMultiplexer);
        verifyZeroInteractions(jgroupsSender);
    }

    @Test
    public void send_metrics_broadcasts() {
        MetricValuesMessage message = new MetricValuesMessage(new HashMap<>());

        clusterClient.sendMetrics(message);

        verify(jgroupsSender).broadcast(message);
        verifyNoMoreInteractions(jgroupsSender);
        verifyZeroInteractions(listenerMultiplexer);
    }

    @Test
    public void close_unregisters_listener() {

        clusterClient.close();

        verify(listenerMultiplexer).removeListenerFor(ActorInputMessage.class);
        verifyNoMoreInteractions(listenerMultiplexer);
        verifyZeroInteractions(jgroupsSender);
    }

    @Test
    public void message_listener_forwards_init() {
        MessageListener messageListener = new MessageListener(runtimeActor);

        messageListener.onMessage(mock(org.jgroups.Message.class), ActorInputMessage.init());

        verify(runtimeActor).init();
        verifyNoMoreInteractions(runtimeActor);
    }

    @Test
    public void message_listener_forwards_dumpMetrics() {
        MessageListener messageListener = new MessageListener(runtimeActor);

        messageListener.onMessage(mock(org.jgroups.Message.class), ActorInputMessage.dumpMetrics());

        verify(runtimeActor).dumpAndFlushMetrics();
        verifyNoMoreInteractions(runtimeActor);
    }

    @Test
    public void message_listener_forwards_sendMessage() {
        MessageListener messageListener = new MessageListener(runtimeActor);
        String from = "from";
        String payload = "payload";

        messageListener.onMessage(mock(org.jgroups.Message.class), ActorInputMessage.sendMessage(from, payload));

        verify(runtimeActor).onMessage(from, payload);
        verifyNoMoreInteractions(runtimeActor);
    }

    @Test
    public void message_listener_forwards_close() {
        MessageListener messageListener = new MessageListener(runtimeActor);

        messageListener.onMessage(mock(org.jgroups.Message.class), ActorInputMessage.close());

        verify(runtimeActor).close();
        verifyNoMoreInteractions(runtimeActor);
    }

}