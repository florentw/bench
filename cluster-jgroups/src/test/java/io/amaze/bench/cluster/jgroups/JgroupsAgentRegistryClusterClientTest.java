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
import io.amaze.bench.cluster.Endpoint;
import io.amaze.bench.cluster.agent.AgentLifecycleMessage;
import io.amaze.bench.cluster.agent.AgentRegistrationMessage;
import io.amaze.bench.cluster.registry.AgentRegistry;
import io.amaze.bench.cluster.registry.AgentRegistryListener;
import io.amaze.bench.cluster.registry.RegisteredAgent;
import io.amaze.bench.shared.jgroups.*;
import io.amaze.bench.shared.util.Network;
import io.amaze.bench.shared.util.SystemConfig;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.stack.IpAddress;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static io.amaze.bench.cluster.agent.AgentLifecycleMessage.closed;
import static io.amaze.bench.cluster.agent.AgentUtil.DUMMY_AGENT;
import static io.amaze.bench.cluster.jgroups.JgroupsAgentRegistryClusterClient.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 10/14/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsAgentRegistryClusterClientTest {

    @Mock
    private JgroupsListenerMultiplexer listenerMultiplexer;
    @Mock
    private JgroupsStateMultiplexer stateMultiplexer;
    @Mock
    private JgroupsViewMultiplexer viewMultiplexer;
    @Mock
    private AgentRegistry agentRegistry;
    @Mock
    private AgentRegistryListener agentRegistryListener;
    @Mock
    private Message message;

    private Address address;
    private Endpoint endpoint;
    private JgroupsAgentRegistryClusterClient registryClusterClient;

    @Before
    public void init() throws UnknownHostException {
        address = new IpAddress(Network.LOCALHOST, 1337);
        endpoint = new JgroupsEndpoint(address);
        registryClusterClient = new JgroupsAgentRegistryClusterClient(listenerMultiplexer,
                                                                      stateMultiplexer,
                                                                      viewMultiplexer,
                                                                      agentRegistry);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(JgroupsViewMultiplexer.class, viewMultiplexer);

        tester.testAllPublicConstructors(JgroupsAgentRegistryClusterClient.class);
        tester.testAllPublicInstanceMethods(registryClusterClient);
    }

    @Test
    public void init_registers_state_holder() {
        verify(stateMultiplexer).addStateHolder(any(JgroupsStateHolder.class));
        verifyNoMoreInteractions(stateMultiplexer);
        verifyZeroInteractions(viewMultiplexer);
        verifyZeroInteractions(listenerMultiplexer);
    }

    @Test
    public void startRegistryListener_adds_registry_listener_and_view_listener() {
        registryClusterClient.startRegistryListener(agentRegistryListener);

        verify(listenerMultiplexer).addListener(eq(AgentLifecycleMessage.class), any(JgroupsListener.class));
        verify(viewMultiplexer).addListener(any(JgroupsViewListener.class));
        verifyNoMoreInteractions(viewMultiplexer);
        verifyNoMoreInteractions(listenerMultiplexer);
    }

    @Test
    public void close_after_startRegistryListener_unregisters_listeners_and_state_holder() {
        registryClusterClient.startRegistryListener(agentRegistryListener);

        registryClusterClient.close();

        verify(stateMultiplexer).removeStateHolder(any(JgroupsStateKey.class));
        verify(viewMultiplexer).removeListener(any(JgroupsViewListener.class));
        verify(listenerMultiplexer).removeListener(any(JgroupsListener.class));
    }

    @Test
    public void close_when_no_call_to_startRegistryListener_was_made_unregisters_state_holder() {
        registryClusterClient.close();

        verify(stateMultiplexer).removeStateHolder(any(JgroupsStateKey.class));
        verifyNoMoreInteractions(viewMultiplexer);
        verifyNoMoreInteractions(listenerMultiplexer);
    }

    @Test
    public void view_listener_forwards_members_disconnection() {
        RegistryViewListener listener = new RegistryViewListener(agentRegistry);

        listener.memberLeft(address);

        verify(agentRegistry).onEndpointDisconnected(new JgroupsEndpoint(address));
        verifyNoMoreInteractions(agentRegistry);
    }

    @Test
    public void view_listener_does_not_forward_other_calls() {
        RegistryViewListener listener = new RegistryViewListener(agentRegistry);

        listener.initialView(Collections.singleton(address));
        listener.memberJoined(address);

        verifyZeroInteractions(agentRegistry);
    }

    @Test
    public void state_holder_returns_valid_key() {
        RegistryStateHolder stateHolder = new RegistryStateHolder(agentRegistry);

        assertThat(stateHolder.getKey(), is(AGENT_REGISTRY_STATE_KEY));
    }

    @Test
    public void state_holder_resets_registry_state_on_state_change() {
        RegistryStateHolder stateHolder = new RegistryStateHolder(agentRegistry);
        Set<RegisteredAgent> registeredAgents = registeredAgents();

        stateHolder.setState(new AgentView(registeredAgents));

        verify(agentRegistry).resetState(registeredAgents);
        verifyNoMoreInteractions(agentRegistry);
    }

    @Test
    public void get_state_on_state_holder_returns_registered_agents() {
        Set<RegisteredAgent> registeredAgents = registeredAgents();
        when(agentRegistry.all()).thenReturn(registeredAgents);
        RegistryStateHolder stateHolder = new RegistryStateHolder(agentRegistry);

        AgentView agentView = stateHolder.getState();

        assertThat(agentView.registeredAgents(), is(registeredAgents));
    }

    @Test
    public void message_listener_forwards_agent_created() throws IOException {
        RegistryMessageListener registryMessageListener = new RegistryMessageListener(agentRegistryListener);
        AgentRegistrationMessage registrationMessage = AgentRegistrationMessage.create(DUMMY_AGENT, endpoint);
        AgentLifecycleMessage created = AgentLifecycleMessage.created(registrationMessage);

        registryMessageListener.onMessage(message, created);

        verify(agentRegistryListener).onAgentRegistration(registrationMessage);
        verifyNoMoreInteractions(agentRegistryListener);
    }

    @Test
    public void message_listener_forwards_agent_failure() throws IOException {
        RegistryMessageListener registryMessageListener = new RegistryMessageListener(agentRegistryListener);
        Throwable throwable = new IllegalArgumentException();

        registryMessageListener.onMessage(message, AgentLifecycleMessage.failed(DUMMY_AGENT, throwable));

        verify(agentRegistryListener).onAgentFailed(eq(DUMMY_AGENT), any(Throwable.class));
        verifyNoMoreInteractions(agentRegistryListener);
    }

    @Test
    public void message_listener_forwards_agent_closed() throws IOException {
        RegistryMessageListener registryMessageListener = new RegistryMessageListener(agentRegistryListener);

        registryMessageListener.onMessage(message, closed(DUMMY_AGENT));

        verify(agentRegistryListener).onAgentSignOff(DUMMY_AGENT);
        verifyNoMoreInteractions(agentRegistryListener);
    }

    private Set<RegisteredAgent> registeredAgents() {
        Set<RegisteredAgent> registeredAgents = new HashSet<>();
        RegisteredAgent registeredAgent = new RegisteredAgent(DUMMY_AGENT,
                                                              SystemConfig.createWithHostname("dummy"),
                                                              0,
                                                              mock(Endpoint.class));
        registeredAgents.add(registeredAgent);
        return registeredAgents;
    }
}