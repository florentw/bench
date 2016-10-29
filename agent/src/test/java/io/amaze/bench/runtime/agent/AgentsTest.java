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
package io.amaze.bench.runtime.agent;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.actor.ActorManagers;
import io.amaze.bench.runtime.cluster.AgentClusterClient;
import io.amaze.bench.runtime.cluster.AgentRegistrySender;
import io.amaze.bench.runtime.cluster.ClusterClientFactory;
import io.amaze.bench.runtime.cluster.ClusterConfigFactory;
import io.amaze.bench.runtime.cluster.registry.AgentRegistry;
import io.amaze.bench.runtime.cluster.registry.AgentRegistryListener;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created on 9/15/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AgentsTest {

    private static final int FUTURE_TIMEOUT = 100;
    private static final AgentKey AGENT = new AgentKey("agent");

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ActorManagers actorManagers;
    @Mock
    private ClusterClientFactory clusterClientFactory;
    @Mock
    private AgentRegistry agentRegistry;
    @Mock
    private AgentClusterClient agentClusterClient;
    @Mock
    private ClusterConfigFactory clusterConfigFactory;
    @Mock
    private Endpoint endpoint;
    @Mock
    private AgentRegistrySender agentRegistrySender;

    private AgentRegistryListener agentRegistryListener;
    private Agents agents;

    @Before
    public void createAgentsAndMockRegistry() {
        agents = new Agents(actorManagers, clusterClientFactory, agentRegistry);
        when(clusterClientFactory.createForAgent(AGENT)).thenReturn(agentClusterClient);
        when(clusterClientFactory.localEndpoint()).thenReturn(endpoint);
        when(agentClusterClient.agentRegistrySender()).thenReturn(agentRegistrySender);

        doAnswer(invocation -> agentRegistryListener = (AgentRegistryListener) invocation.getArguments()[0]).when(
                agentRegistry).addListener(any(AgentRegistryListener.class));
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorManagers.class, actorManagers);

        tester.testAllPublicConstructors(Agents.class);
        tester.testAllPublicInstanceMethods(agents);
    }

    @Test
    public void creating_agent_adds_listener() {

        Future<Agent> future = agents.create(AGENT);

        assertNotNull(future);
        InOrder inOrder = inOrder(agentRegistry, actorManagers);
        inOrder.verify(agentRegistry).addListener(any(AgentRegistryListener.class));
        inOrder.verify(actorManagers).createEmbedded(AGENT, clusterClientFactory);
        inOrder.verify(actorManagers).createForked(AGENT, clusterClientFactory.clusterConfigFactory());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void agent_is_returned_when_the_correct_agent_registers() throws ExecutionException, InterruptedException {
        Future<Agent> future = agents.create(AGENT);

        agentRegistryListener.onAgentRegistration(AgentRegistrationMessage.create(AGENT, endpoint));

        Agent agent = getUninterruptibly(future);
        assertNotNull(agent);
        assertThat(agent.getKey()).is(AGENT);

        InOrder inOrder = inOrder(agentRegistry);
        inOrder.verify(agentRegistry).addListener(agentRegistryListener);
        inOrder.verify(agentRegistry).removeListener(agentRegistryListener);
        inOrder.verifyNoMoreInteractions();
    }

    @Test(expected = TimeoutException.class)
    public void agent_is_not_returned_when_the_wrong_agent_registers()
            throws ExecutionException, InterruptedException, TimeoutException {
        Future<Agent> future = agents.create(AGENT);

        agentRegistryListener.onAgentRegistration(AgentRegistrationMessage.create(new AgentKey("another-agent"),
                                                                                  endpoint));

        getUninterruptibly(future, FUTURE_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Test
    public void agent_signOff_sets_AgentSignOffException()
            throws ExecutionException, InterruptedException, TimeoutException {
        Future<Agent> future = agents.create(AGENT);

        agentRegistryListener.onAgentSignOff(AGENT);

        InOrder inOrder = inOrder(agentRegistry);
        inOrder.verify(agentRegistry).addListener(agentRegistryListener);
        inOrder.verify(agentRegistry).removeListener(agentRegistryListener);
        inOrder.verifyNoMoreInteractions();

        expectedException.expect(ExecutionException.class);
        expectedException.expectCause(instanceOf(Agents.AgentSignOffException.class));
        getUninterruptibly(future, FUTURE_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Test
    public void agent_failure_sets_throwable() throws ExecutionException, InterruptedException, TimeoutException {
        Exception throwable = new IllegalArgumentException();
        Future<Agent> future = agents.create(AGENT);

        agentRegistryListener.onAgentFailed(AGENT, throwable);

        InOrder inOrder = inOrder(agentRegistry);
        inOrder.verify(agentRegistry).addListener(agentRegistryListener);
        inOrder.verify(agentRegistry).removeListener(agentRegistryListener);
        inOrder.verifyNoMoreInteractions();

        expectedException.expect(ExecutionException.class);
        expectedException.expectCause(instanceOf(Agents.AgentFailureException.class));
        getUninterruptibly(future, FUTURE_TIMEOUT, TimeUnit.MILLISECONDS);
    }

}