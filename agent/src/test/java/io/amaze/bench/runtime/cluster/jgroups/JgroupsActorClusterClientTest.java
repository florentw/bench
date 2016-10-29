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
package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.actor.ActorInputMessage;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.actor.RuntimeActor;
import io.amaze.bench.runtime.actor.TestActor;
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
        when(runtimeActor.getKey()).thenReturn(DUMMY_ACTOR);
        clusterClient = new JgroupsActorClusterClient(endpoint, listenerMultiplexer, jgroupsSender, actorRegistry);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorInputMessage.class, ActorInputMessage.dumpMetrics());
        tester.setDefault(ActorKey.class, DUMMY_ACTOR);
        tester.setDefault(JgroupsSender.class, jgroupsSender);

        tester.testAllPublicConstructors(JgroupsActorClusterClient.class);
        tester.testAllPublicInstanceMethods(clusterClient);
    }

    @Test
    public void starting_actor_listener_registers_it() {

        clusterClient.startActorListener(runtimeActor);

        verify(listenerMultiplexer).addListener(eq(JgroupsActorMessage.class), any(MessageListener.class));
        verifyNoMoreInteractions(listenerMultiplexer);
        verifyZeroInteractions(jgroupsSender);
    }

    @Test
    public void send_metrics_broadcasts() {
        MetricValuesMessage message = new MetricValuesMessage(TestActor.DUMMY_ACTOR, new HashMap<>());

        clusterClient.sendMetrics(message);

        verify(jgroupsSender).broadcast(message);
        verifyNoMoreInteractions(jgroupsSender);
        verifyZeroInteractions(listenerMultiplexer);
    }

    @Test
    public void close_unregisters_listener() {

        clusterClient.close();

        verify(listenerMultiplexer).removeListener(any(MessageListener.class));
        verifyNoMoreInteractions(listenerMultiplexer);
        verifyZeroInteractions(jgroupsSender);
    }

    @Test
    public void message_listener_forwards_dumpMetrics() {
        MessageListener messageListener = new MessageListener(runtimeActor);

        messageListener.onMessage(mock(org.jgroups.Message.class), createMessage(ActorInputMessage.dumpMetrics()));

        verify(runtimeActor).getKey();
        verify(runtimeActor).dumpAndFlushMetrics();
        verifyNoMoreInteractions(runtimeActor);
    }

    @Test
    public void message_listener_forwards_sendMessage() {
        MessageListener messageListener = new MessageListener(runtimeActor);
        String from = "from";
        String payload = "payload";

        messageListener.onMessage(mock(org.jgroups.Message.class),
                                  createMessage(ActorInputMessage.sendMessage(from, payload)));

        verify(runtimeActor).getKey();
        verify(runtimeActor).onMessage(from, payload);
        verifyNoMoreInteractions(runtimeActor);
    }

    @Test
    public void message_listener_forwards_close() {
        MessageListener messageListener = new MessageListener(runtimeActor);

        messageListener.onMessage(mock(org.jgroups.Message.class), createMessage(ActorInputMessage.close()));

        verify(runtimeActor).getKey();
        verify(runtimeActor).close();
        verifyNoMoreInteractions(runtimeActor);
    }

    private JgroupsActorMessage createMessage(ActorInputMessage input) {
        return new JgroupsActorMessage(DUMMY_ACTOR, input);
    }

}