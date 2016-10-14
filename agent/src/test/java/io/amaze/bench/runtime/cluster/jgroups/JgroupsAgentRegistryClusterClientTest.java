package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.agent.AgentLifecycleMessage;
import io.amaze.bench.runtime.agent.AgentRegistrationMessage;
import io.amaze.bench.runtime.cluster.registry.AgentRegistry;
import io.amaze.bench.runtime.cluster.registry.AgentRegistryListener;
import io.amaze.bench.runtime.cluster.registry.RegisteredAgent;
import io.amaze.bench.shared.jgroups.*;
import io.amaze.bench.shared.metric.SystemConfig;
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

import static io.amaze.bench.runtime.agent.AgentLifecycleMessage.closed;
import static io.amaze.bench.runtime.agent.AgentTest.DUMMY_AGENT;
import static io.amaze.bench.runtime.cluster.jgroups.JgroupsAgentRegistryClusterClient.AGENT_REGISTRY_STATE_KEY;
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
        address = new IpAddress("localhost", 1337);
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
        verify(listenerMultiplexer).removeListenerFor(AgentLifecycleMessage.class);
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
        JgroupsAgentRegistryClusterClient.RegistryViewListener listener = new JgroupsAgentRegistryClusterClient.RegistryViewListener(
                agentRegistry);

        listener.memberLeft(address);

        verify(agentRegistry).onEndpointDisconnected(new JgroupsEndpoint(address));
        verifyNoMoreInteractions(agentRegistry);
    }

    @Test
    public void view_listener_does_not_forward_other_calls() {
        JgroupsAgentRegistryClusterClient.RegistryViewListener listener = new JgroupsAgentRegistryClusterClient.RegistryViewListener(
                agentRegistry);

        listener.initialView(Collections.singleton(address));
        listener.memberJoined(address);

        verifyZeroInteractions(agentRegistry);
    }

    @Test
    public void state_holder_returns_valid_key() {
        JgroupsAgentRegistryClusterClient.RegistryStateHolder stateHolder = new JgroupsAgentRegistryClusterClient.RegistryStateHolder(
                agentRegistry);

        assertThat(stateHolder.getKey(), is(AGENT_REGISTRY_STATE_KEY));
    }

    @Test
    public void state_holder_resets_registry_state_on_state_change() {
        JgroupsAgentRegistryClusterClient.RegistryStateHolder stateHolder = new JgroupsAgentRegistryClusterClient.RegistryStateHolder(
                agentRegistry);
        Set<RegisteredAgent> registeredAgents = registeredAgents();

        stateHolder.setState(new AgentView(registeredAgents));

        verify(agentRegistry).resetState(registeredAgents);
        verifyNoMoreInteractions(agentRegistry);
    }

    @Test
    public void get_state_on_state_holder_returns_registered_agents() {
        Set<RegisteredAgent> registeredAgents = registeredAgents();
        when(agentRegistry.all()).thenReturn(registeredAgents);
        JgroupsAgentRegistryClusterClient.RegistryStateHolder stateHolder = new JgroupsAgentRegistryClusterClient.RegistryStateHolder(
                agentRegistry);

        AgentView agentView = stateHolder.getState();

        assertThat(agentView.getRegisteredAgents(), is(registeredAgents));
    }

    @Test
    public void message_listener_forwards_agent_created() throws IOException {
        JgroupsAgentRegistryClusterClient.RegistryMessageListener registryMessageListener = new JgroupsAgentRegistryClusterClient.RegistryMessageListener(
                agentRegistryListener);
        AgentRegistrationMessage registrationMessage = AgentRegistrationMessage.create(DUMMY_AGENT, endpoint);
        AgentLifecycleMessage created = AgentLifecycleMessage.created(registrationMessage);

        registryMessageListener.onMessage(message, created);

        verify(agentRegistryListener).onAgentRegistration(registrationMessage);
        verifyNoMoreInteractions(agentRegistryListener);
    }

    @Test
    public void message_listener_forwards_agent_failure() throws IOException {
        JgroupsAgentRegistryClusterClient.RegistryMessageListener registryMessageListener = new JgroupsAgentRegistryClusterClient.RegistryMessageListener(
                agentRegistryListener);
        Throwable throwable = new IllegalArgumentException();

        registryMessageListener.onMessage(message, AgentLifecycleMessage.failed(DUMMY_AGENT, throwable));

        verify(agentRegistryListener).onAgentFailed(eq(DUMMY_AGENT), any(Throwable.class));
        verifyNoMoreInteractions(agentRegistryListener);
    }

    @Test
    public void message_listener_forwards_agent_closed() throws IOException {
        JgroupsAgentRegistryClusterClient.RegistryMessageListener registryMessageListener = new JgroupsAgentRegistryClusterClient.RegistryMessageListener(
                agentRegistryListener);

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