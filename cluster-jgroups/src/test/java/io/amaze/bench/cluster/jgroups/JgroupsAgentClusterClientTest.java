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
package io.amaze.bench.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.actor.ActorCreationRequest;
import io.amaze.bench.cluster.actor.ActorInputMessage;
import io.amaze.bench.cluster.actor.ActorSender;
import io.amaze.bench.cluster.agent.AgentClientListener;
import io.amaze.bench.cluster.agent.AgentInputMessage;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.shared.jgroups.JgroupsListenerMultiplexer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 10/14/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsAgentClusterClientTest {

    private static final AgentKey DUMMY_AGENT = new AgentKey("agent");

    @Mock
    private JgroupsListenerMultiplexer listenerMultiplexer;
    @Mock
    private JgroupsSender jgroupsSender;
    @Mock
    private AgentClientListener agentClientListener;
    @Mock
    private ActorRegistry actorRegistry;

    private JgroupsAgentClusterClient clusterClient;

    @Before
    public void init() {
        clusterClient = new JgroupsAgentClusterClient(listenerMultiplexer, jgroupsSender, actorRegistry);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorInputMessage.class, ActorInputMessage.dumpMetrics());
        tester.setDefault(ActorKey.class, DUMMY_ACTOR);
        tester.setDefault(AgentKey.class, DUMMY_AGENT);

        tester.testAllPublicConstructors(JgroupsAgentClusterClient.class);
        tester.testAllPublicInstanceMethods(clusterClient);
    }

    @Test
    public void starting_agent_listener_registers_it() {

        clusterClient.startAgentListener(DUMMY_AGENT, agentClientListener);

        verify(listenerMultiplexer).addListener(eq(AgentInputMessage.class),
                                                any(JgroupsAgentClusterClient.MessageListener.class));
        verifyNoMoreInteractions(listenerMultiplexer);
        verifyZeroInteractions(jgroupsSender);
    }

    @Test
    public void close_unregisters_listener() {

        clusterClient.close();

        verify(listenerMultiplexer).removeListener(any(JgroupsAgentClusterClient.MessageListener.class));
        verifyNoMoreInteractions(listenerMultiplexer);
        verifyZeroInteractions(jgroupsSender);
    }

    @Test
    public void message_listener_forwards_createActor() {
        JgroupsAgentClusterClient.MessageListener messageListener = new JgroupsAgentClusterClient.MessageListener(
                DUMMY_AGENT,
                agentClientListener);

        messageListener.onMessage(mock(org.jgroups.Message.class),
                                  AgentInputMessage.createActor(DUMMY_AGENT,
                                                                new ActorCreationRequest(TestActor.DUMMY_CONFIG)));

        verify(agentClientListener).onActorCreationRequest(TestActor.DUMMY_CONFIG);
        verifyNoMoreInteractions(agentClientListener);
    }

    @Test
    public void message_listener_forwards_closeActor() {
        JgroupsAgentClusterClient.MessageListener messageListener = new JgroupsAgentClusterClient.MessageListener(
                DUMMY_AGENT,
                agentClientListener);

        messageListener.onMessage(mock(org.jgroups.Message.class),
                                  AgentInputMessage.closeActor(DUMMY_AGENT, DUMMY_ACTOR));

        verify(agentClientListener).onActorCloseRequest(DUMMY_ACTOR);
        verifyNoMoreInteractions(agentClientListener);
    }

    @Test
    public void message_listener_does_not_forward_for_another_agent() {
        JgroupsAgentClusterClient.MessageListener messageListener = new JgroupsAgentClusterClient.MessageListener(
                DUMMY_AGENT,
                agentClientListener);

        messageListener.onMessage(mock(org.jgroups.Message.class),
                                  AgentInputMessage.createActor(new AgentKey("other-agent"),
                                                                new ActorCreationRequest(TestActor.DUMMY_CONFIG)));

        verifyNoMoreInteractions(agentClientListener);
    }

    @Test
    public void sender_returns_new_instance() {
        ActorSender actorSender1 = clusterClient.actorSender();
        ActorSender actorSender2 = clusterClient.actorSender();

        assertNotNull(actorSender1);
        assertNotNull(actorSender2);
        assertNotSame(actorSender1, actorSender2);
    }

}