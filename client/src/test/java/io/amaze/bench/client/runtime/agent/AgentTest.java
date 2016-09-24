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
package io.amaze.bench.client.runtime.agent;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.client.runtime.actor.*;
import io.amaze.bench.client.runtime.cluster.ActorClusterClient;
import io.amaze.bench.client.runtime.cluster.AgentClusterClient;
import io.amaze.bench.client.runtime.cluster.ClusterClientFactory;
import io.amaze.bench.shared.jms.JMSEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.client.runtime.actor.ActorLifecycleMessage.Phase;
import static io.amaze.bench.client.runtime.actor.TestActor.*;
import static io.amaze.bench.util.Matchers.isLifecyclePhase;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created on 3/3/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AgentTest {

    public static final String DUMMY_AGENT = "DUMMY_AGENT";

    @Mock
    private ActorClusterClient actorClient;
    @Mock
    private AgentClusterClient agentClient;

    private ActorManager embeddedManager;
    @Mock
    private ActorManager forkedManager;
    @Mock
    private ActorManagers actorManagers;
    @Mock
    private ManagedActor embeddedManagedActor;
    @Mock
    private ManagedActor forkedManagedActor;

    private Agent agent;
    private ClusterClientFactory clientFactory;

    @Before
    public void before() throws ValidationException {
        clientFactory = new DummyClientFactory(agentClient, actorClient);
        embeddedManager = spy(new EmbeddedActorManager(DUMMY_AGENT, new Actors(clientFactory)));

        when(actorManagers.createEmbedded(anyString(), any(ClusterClientFactory.class))).thenReturn(embeddedManager);
        when(actorManagers.createForked(anyString())).thenReturn(forkedManager);
        doReturn(embeddedManagedActor).when(embeddedManager).createActor(TestActor.DUMMY_CONFIG);
        doReturn(forkedManagedActor).when(forkedManager).createActor(TestActor.DUMMY_CONFIG);

        agent = new Agent(DUMMY_AGENT, clientFactory, actorManagers);

        start_agent_registers_properly();
    }

    @After
    public void after() throws Exception {
        agent.close();
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorManagers.class, new ActorManagers(new JMSEndpoint("dummy", 10)));

        tester.testAllPublicConstructors(Agent.class);
        tester.testAllPublicInstanceMethods(agent);
    }

    @Test
    public void agent_is_created_properly() {
        // Smoke tests
        assertNotNull(agent);
        assertNotNull(agent.getName());

        // Check ActorManagers created
        verify(actorManagers).createEmbedded(agent.getName(), clientFactory);
        verify(actorManagers).createForked(agent.getName());
        verifyNoMoreInteractions(actorManagers);

        verifyNoMoreInteractions(agentClient);
    }

    @Test
    public void create_actor_registers_and_sends_lifecycle_msg() throws Exception {

        agent.onActorCreationRequest(DUMMY_CONFIG);

        assertThat(agent.getActors().size(), is(1));
        assertThat(agent.getActors().iterator().next(), is(embeddedManagedActor));

        verify(embeddedManager).createActor(DUMMY_CONFIG);
        verifyZeroInteractions(forkedManager);

        verify(agentClient).sendToActorRegistry(argThat(isLifecyclePhase(Phase.CREATED)));
        verifyNoMoreInteractions(agentClient);
        verifyNoMoreInteractions(actorClient);
    }

    @Test
    public void create_forked_actor_calls_the_right_manager() throws Exception {

        ActorConfig actorConfig = configForActor(TestActor.class, true);
        agent.onActorCreationRequest(actorConfig);

        // Check managers interactions
        verifyZeroInteractions(embeddedManager);
        verify(forkedManager).createActor(actorConfig);
    }

    @Test
    public void create_then_close_actor() throws Exception {

        agent.onActorCreationRequest(DUMMY_CONFIG);

        agent.onActorCloseRequest(DUMMY_ACTOR);

        // Check good flow of messages
        verify(agentClient).sendToActorRegistry(argThat(isLifecyclePhase(Phase.CREATED)));
        verify(embeddedManagedActor).close();
        verifyNoMoreInteractions(embeddedManagedActor);
        verifyNoMoreInteractions(agentClient);
    }

    @Test
    public void close_unknown_actor_does_nothing() throws Exception {

        agent.onActorCloseRequest(DUMMY_ACTOR);

        assertThat(agent.getActors().isEmpty(), is(true));
        verifyNoMoreInteractions(actorClient);
        verifyNoMoreInteractions(agentClient);
    }

    @Test
    public void close_actor_failure() throws Exception {
        agent.onActorCreationRequest(configForActor(TestActorAfterThrows.class));

        agent.onActorCloseRequest(DUMMY_ACTOR);

        InOrder inOrder = inOrder(actorClient, agentClient);
        inOrder.verify(actorClient).startActorListener(any(RuntimeActor.class));
        inOrder.verify(agentClient).sendToActorRegistry(argThat(isLifecyclePhase(Phase.CREATED)));
        inOrder.verify(actorClient).sendToActorRegistry(argThat(isLifecyclePhase(Phase.FAILED)));
        inOrder.verify(actorClient).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void closing_agent_closes_actors_and_unregisters() throws Exception {
        agent.onActorCreationRequest(DUMMY_CONFIG);

        agent.close();

        assertThat(agent.getActors().isEmpty(), is(true));

        InOrder inOrder = inOrder(agentClient, actorClient, embeddedManagedActor);
        inOrder.verify(agentClient).sendToActorRegistry(argThat(isLifecyclePhase(Phase.CREATED)));
        inOrder.verify(embeddedManagedActor).close();
        inOrder.verify(agentClient).sendToAgentRegistry(argThat(isAgentRegistry(AgentOutputMessage.Action.UNREGISTER_AGENT)));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void create_same_actor_twice_registers_only_one() throws Exception {

        agent.onActorCreationRequest(DUMMY_CONFIG);
        agent.onActorCreationRequest(DUMMY_CONFIG);
        assertThat(agent.getActors().size(), is(1));

        verify(agentClient).sendToActorRegistry(argThat(isLifecyclePhase(Phase.CREATED)));
        verifyNoMoreInteractions(actorClient);
        verifyNoMoreInteractions(agentClient);
    }

    @Test
    public void create_invalid_actor() throws Exception {

        agent.onActorCreationRequest(configForActor(String.class));

        assertThat(agent.getActors().isEmpty(), is(true));
        verify(agentClient).sendToActorRegistry(argThat(isLifecyclePhase(Phase.FAILED)));
        verifyNoMoreInteractions(actorClient);
        verifyNoMoreInteractions(agentClient);
    }

    private void start_agent_registers_properly() {
        verify(agentClient).startAgentListener(eq(DUMMY_AGENT), any(AgentClientListener.class));
        verify(agentClient).sendToAgentRegistry(argThat(isAgentRegistry(AgentOutputMessage.Action.REGISTER_AGENT)));
    }

    private ArgumentMatcher<AgentOutputMessage> isAgentRegistry(final AgentOutputMessage.Action action) {
        return new ArgumentMatcher<AgentOutputMessage>() {
            @Override
            public boolean matches(final Object argument) {
                AgentOutputMessage message = (AgentOutputMessage) argument;
                return message.getAction() == action;
            }
        };
    }
}