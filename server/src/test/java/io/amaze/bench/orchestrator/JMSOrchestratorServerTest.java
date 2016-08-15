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
import io.amaze.bench.client.runtime.agent.AgentInputMessage;
import io.amaze.bench.client.runtime.agent.AgentInputMessage.Action;
import io.amaze.bench.orchestrator.registry.ActorRegistryListener;
import io.amaze.bench.orchestrator.registry.AgentRegistryListener;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.jms.MessageListener;
import java.io.Serializable;

import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.client.runtime.agent.AgentTest.DUMMY_AGENT;
import static io.amaze.bench.client.runtime.agent.Constants.AGENTS_ACTOR_NAME;
import static io.amaze.bench.client.runtime.agent.Constants.MASTER_ACTOR_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 4/3/16.
 */
public final class JMSOrchestratorServerTest {

    private static final String DUMMY_MSG = "dummy-msg";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private JMSOrchestratorServer server;
    private JMSServer jmsServer;
    private JMSClient jmsClient;

    @Before
    public void before() {
        jmsServer = mock(JMSServer.class);
        jmsClient = mock(JMSClient.class);

        server = new JMSOrchestratorServer(jmsServer, jmsClient);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(JMSOrchestratorServer.class);
        tester.testAllPublicInstanceMethods(server);
    }

    @Test
    public void actor_and_agents_endpoints_created_when_orchestrator_server_created() throws JMSException {
        verify(jmsServer).createQueue(MASTER_ACTOR_NAME);
        verify(jmsServer).createTopic(AGENTS_ACTOR_NAME);

        verifyNoMoreInteractions(jmsServer, jmsClient);
    }

    @Test
    public void start_registry_listeners() throws JMSException {
        AgentRegistryListener agentRegistryListener = mock(AgentRegistryListener.class);
        ActorRegistryListener actorRegistryListener = mock(ActorRegistryListener.class);

        server.startRegistryListeners(agentRegistryListener, actorRegistryListener);

        verify(jmsClient).addQueueListener(eq(MASTER_ACTOR_NAME), any(MessageListener.class));
        verify(jmsClient).startListening();
    }

    @Test
    public void create_actor_queue() throws JMSException {
        server.createActorQueue(DUMMY_ACTOR);

        verify(jmsServer).createQueue(DUMMY_ACTOR);
    }

    @Test
    public void create_actor_queue_fails_and_rethrows() throws JMSException {
        JMSException expectedCause = new JMSException(null);
        doThrow(expectedCause).when(jmsServer).createQueue(any(String.class));

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(expectedCause));

        server.createActorQueue(DUMMY_ACTOR);
    }

    @Test
    public void delete_actor_queue() throws JMSException {
        server.deleteActorQueue(DUMMY_ACTOR);

        verify(jmsServer).deleteQueue(DUMMY_ACTOR);
    }

    @Test
    public void send_to_actor() throws JMSException {
        server.sendToActor(DUMMY_ACTOR, DUMMY_MSG);

        verify(jmsClient).sendToQueue(DUMMY_ACTOR, DUMMY_MSG);
    }

    @Test
    public void send_to_actor_fails_and_rethrows() throws JMSException {
        JMSException expectedCause = new JMSException(null);

        doThrow(expectedCause).when(jmsClient).sendToQueue(any(String.class), any(Serializable.class));
        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(expectedCause));

        server.sendToActor(DUMMY_ACTOR, DUMMY_MSG);
    }

    @Test
    public void send_to_agent() throws JMSException {
        AgentInputMessage msg = new AgentInputMessage(DUMMY_AGENT, Action.CREATE_ACTOR, DUMMY_MSG);
        server.sendToAgent(msg);

        verify(jmsClient).sendToTopic(AGENTS_ACTOR_NAME, msg);
    }

    @Test
    public void send_to_agent_fails_and_rethrows() throws JMSException {
        JMSException expectedCause = new JMSException(null);
        doThrow(expectedCause).when(jmsClient).sendToTopic(any(String.class), any(Serializable.class));

        AgentInputMessage msg = new AgentInputMessage(DUMMY_AGENT, Action.CREATE_ACTOR, DUMMY_MSG);

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(expectedCause));

        server.sendToAgent(msg);
    }

    @Test
    public void close_closes_client_and_server() throws JMSException {
        server.close();

        verify(jmsClient).close();
        verify(jmsServer).close();
    }

    @After
    public void after() {
        server.close();
    }

}