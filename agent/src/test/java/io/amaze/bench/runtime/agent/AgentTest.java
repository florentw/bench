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
package io.amaze.bench.runtime.agent;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.cluster.AgentClusterClientFactory;
import io.amaze.bench.cluster.ClusterConfigFactory;
import io.amaze.bench.cluster.Endpoint;
import io.amaze.bench.cluster.actor.*;
import io.amaze.bench.cluster.agent.*;
import io.amaze.bench.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.runtime.actor.*;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.shared.test.Json;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.cluster.actor.ActorLifecycleMessage.State;
import static io.amaze.bench.runtime.actor.TestActor.*;
import static io.amaze.bench.util.Matchers.isActorState;
import static io.amaze.bench.util.Matchers.isAgentState;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created on 3/3/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AgentTest {

    public static final AgentKey DUMMY_AGENT = new AgentKey("DUMMY_AGENT");

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
    @Mock
    private ActorRegistryClusterClient actorRegistryClient;
    @Mock
    private ClusterConfigFactory clusterConfigFactory;
    @Mock
    private ActorRegistrySender actorRegistrySender;
    @Mock
    private ActorRegistrySender agentActorRegistrySender;
    @Mock
    private AgentRegistrySender agentRegistrySender;
    @Mock
    private Endpoint localEndpoint;

    private Agent agent;
    private AgentClusterClientFactory clientFactory;

    @Before
    public void before() throws ValidationException {
        clientFactory = new DummyClientFactory(localEndpoint,
                                               agentClient,
                                               actorClient,
                                               actorRegistryClient,
                                               clusterConfigFactory,
                                               null);
        embeddedManager = spy(new EmbeddedActorManager(clientFactory));

        when(actorClient.localEndpoint()).thenReturn(localEndpoint);
        when(actorManagers.createEmbedded(any(AgentKey.class), any(AgentClusterClientFactory.class))).thenReturn(
                embeddedManager);
        when(actorManagers.createForked(any(AgentKey.class), eq(clusterConfigFactory))).thenReturn(forkedManager);
        doReturn(embeddedManagedActor).when(embeddedManager).createActor(DUMMY_CONFIG);
        doReturn(forkedManagedActor).when(forkedManager).createActor(DUMMY_CONFIG);

        doReturn(agentActorRegistrySender).when(agentClient).actorRegistrySender();
        doReturn(actorRegistrySender).when(actorClient).actorRegistrySender();
        doReturn(agentRegistrySender).when(agentClient).agentRegistrySender();

        agent = new Agent(DUMMY_AGENT, clientFactory, actorManagers);

        start_agent_registers_properly();
    }

    @After
    public void after() throws Exception {
        agent.close();
    }

    @Test
    public void toString_yields_valid_json() {
        assertTrue(Json.isValid(agent.toString()));
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorManagers.class, new ActorManagers());
        tester.setDefault(AgentKey.class, DUMMY_AGENT);

        tester.testAllPublicConstructors(Agent.class);
        tester.testAllPublicInstanceMethods(agent);
    }

    @Test
    public void agent_is_created_properly() {
        // Smoke tests
        assertNotNull(agent);
        assertNotNull(agent.getKey());

        // Check ActorManagers created
        verify(actorManagers).createEmbedded(agent.getKey(), clientFactory);
        verify(actorManagers).createForked(agent.getKey(), clusterConfigFactory);
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

        verify(agentActorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.CREATED)));
        verifyNoMoreInteractions(agentActorRegistrySender);
        verifyNoMoreInteractions(actorRegistrySender);
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
        verify(agentActorRegistrySender).send(argThat(isActorState(State.CREATED)));
        verify(embeddedManagedActor).close();
        verifyNoMoreInteractions(embeddedManagedActor);
        verifyNoMoreInteractions(agentActorRegistrySender);
        verifyNoMoreInteractions(actorRegistrySender);
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

        InOrder inOrder = inOrder(actorClient, agentActorRegistrySender, actorRegistrySender);
        inOrder.verify(actorClient).startActorListener(any(RuntimeActor.class));
        inOrder.verify(actorRegistrySender).send(argThat(isActorState(State.FAILED)));
        inOrder.verify(actorClient).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void closing_agent_closes_actors_and_unregisters() throws Exception {
        agent.onActorCreationRequest(DUMMY_CONFIG);

        agent.close();

        assertThat(agent.getActors().isEmpty(), is(true));

        InOrder inOrder = inOrder(agentClient,
                                  agentActorRegistrySender,
                                  agentRegistrySender,
                                  embeddedManagedActor,
                                  forkedManager,
                                  embeddedManager);

        inOrder.verify(agentActorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.CREATED)));
        inOrder.verify(embeddedManagedActor).close();
        inOrder.verify(agentRegistrySender).send(argThat(isAgentState(AgentLifecycleMessage.State.CLOSED)));
        inOrder.verify(forkedManager).close();
        inOrder.verify(embeddedManager).close();
        inOrder.verify(agentClient).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void closing_agent_twice_closes_only_once() {
        agent.onActorCreationRequest(DUMMY_CONFIG);

        agent.close();
        agent.close();

        verify(agentClient).close();
    }

    @Test
    public void create_same_actor_twice_registers_only_one() throws Exception {

        agent.onActorCreationRequest(DUMMY_CONFIG);
        agent.onActorCreationRequest(DUMMY_CONFIG);
        assertThat(agent.getActors().size(), is(1));

        verify(agentActorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.CREATED)));
        verifyNoMoreInteractions(actorRegistrySender);
        verifyNoMoreInteractions(agentActorRegistrySender);
    }

    @Test
    public void create_invalid_actor() throws Exception {

        agent.onActorCreationRequest(configForActor(String.class));

        assertThat(agent.getActors().isEmpty(), is(true));
        verify(agentActorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.FAILED)));
        verifyNoMoreInteractions(actorClient);
        verifyNoMoreInteractions(actorRegistrySender);
    }

    private void start_agent_registers_properly() {
        verify(agentClient).startAgentListener(eq(DUMMY_AGENT), any(AgentClientListener.class));
        verify(agentRegistrySender).send(argThat(isAgentState(AgentLifecycleMessage.State.CREATED)));
        verify(agentClient).agentRegistrySender();
    }
}