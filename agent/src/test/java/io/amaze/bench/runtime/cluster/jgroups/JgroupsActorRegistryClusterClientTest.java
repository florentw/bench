package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.actor.ActorDeployInfo;
import io.amaze.bench.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryListener;
import io.amaze.bench.runtime.cluster.registry.RegisteredActor;
import io.amaze.bench.shared.jgroups.*;
import io.amaze.bench.shared.util.Network;
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

import static io.amaze.bench.runtime.actor.ActorLifecycleMessage.*;
import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.runtime.agent.AgentTest.DUMMY_AGENT;
import static io.amaze.bench.runtime.cluster.jgroups.JgroupsActorRegistryClusterClient.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 10/9/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsActorRegistryClusterClientTest {

    @Mock
    private JgroupsListenerMultiplexer listenerMultiplexer;
    @Mock
    private JgroupsStateMultiplexer stateMultiplexer;
    @Mock
    private JgroupsViewMultiplexer viewMultiplexer;
    @Mock
    private ActorRegistry actorRegistry;
    @Mock
    private ActorRegistryListener actorRegistryListener;
    @Mock
    private Message message;

    private Address address;
    private Endpoint endpoint;
    private JgroupsActorRegistryClusterClient registryClusterClient;

    @Before
    public void init() throws UnknownHostException {
        address = new IpAddress(Network.LOCALHOST, 1337);
        endpoint = new JgroupsEndpoint(address);
        registryClusterClient = new JgroupsActorRegistryClusterClient(listenerMultiplexer,
                                                                      stateMultiplexer,
                                                                      viewMultiplexer,
                                                                      actorRegistry);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(JgroupsViewMultiplexer.class, viewMultiplexer);

        tester.testAllPublicConstructors(JgroupsActorRegistryClusterClient.class);
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
        registryClusterClient.startRegistryListener(actorRegistryListener);

        verify(listenerMultiplexer).addListener(eq(ActorLifecycleMessage.class), any(JgroupsListener.class));
        verify(viewMultiplexer).addListener(any(JgroupsViewListener.class));
        verifyNoMoreInteractions(viewMultiplexer);
        verifyNoMoreInteractions(listenerMultiplexer);
    }

    @Test
    public void close_after_startRegistryListener_unregisters_listeners_and_state_holder() {
        registryClusterClient.startRegistryListener(actorRegistryListener);

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
        ActorRegistryViewListener listener = new ActorRegistryViewListener(actorRegistry);

        listener.memberLeft(address);

        verify(actorRegistry).onEndpointDisconnected(new JgroupsEndpoint(address));
        verifyNoMoreInteractions(actorRegistry);
    }

    @Test
    public void view_listener_does_not_forward_other_calls() {
        ActorRegistryViewListener listener = new ActorRegistryViewListener(actorRegistry);

        listener.initialView(Collections.singleton(address));
        listener.memberJoined(address);

        verifyZeroInteractions(actorRegistry);
    }

    @Test
    public void state_holder_returns_valid_key() {
        RegistryStateHolder stateHolder = new RegistryStateHolder(actorRegistry);

        assertThat(stateHolder.getKey(), is(ACTOR_REGISTRY_STATE_KEY));
    }

    @Test
    public void state_holder_resets_registry_state_on_state_change() {
        RegistryStateHolder stateHolder = new RegistryStateHolder(actorRegistry);
        Set<RegisteredActor> registeredActors = registeredActors();

        stateHolder.setState(new ActorView(registeredActors));

        verify(actorRegistry).resetState(registeredActors);
        verifyNoMoreInteractions(actorRegistry);
    }

    @Test
    public void get_state_on_state_holder_returns_registered_actors() {
        Set<RegisteredActor> registeredActors = registeredActors();
        when(actorRegistry.all()).thenReturn(registeredActors);
        RegistryStateHolder stateHolder = new RegistryStateHolder(actorRegistry);

        ActorView actorView = stateHolder.getState();

        assertThat(actorView.getRegisteredActors(), is(registeredActors));
    }

    @Test
    public void message_listener_forwards_actor_created() throws IOException {
        RegistryMessageListener registryMessageListener = new RegistryMessageListener(actorRegistryListener);

        registryMessageListener.onMessage(message, created(DUMMY_ACTOR, DUMMY_AGENT));

        verify(actorRegistryListener).onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        verifyNoMoreInteractions(actorRegistryListener);
    }

    @Test
    public void message_listener_forwards_actor_initialized() throws IOException {
        RegistryMessageListener registryMessageListener = new RegistryMessageListener(actorRegistryListener);
        ActorDeployInfo deployInfo = new ActorDeployInfo(endpoint, 10);

        registryMessageListener.onMessage(message, initialized(DUMMY_ACTOR, deployInfo));

        verify(actorRegistryListener).onActorInitialized(DUMMY_ACTOR, deployInfo);
        verifyNoMoreInteractions(actorRegistryListener);
    }

    @Test
    public void message_listener_forwards_actor_failure() throws IOException {
        RegistryMessageListener registryMessageListener = new RegistryMessageListener(actorRegistryListener);
        Throwable throwable = new IllegalArgumentException();

        registryMessageListener.onMessage(message, failed(DUMMY_ACTOR, throwable));

        verify(actorRegistryListener).onActorFailed(eq(DUMMY_ACTOR), any(Throwable.class));
        verifyNoMoreInteractions(actorRegistryListener);
    }

    @Test
    public void message_listener_forwards_actor_closed() throws IOException {
        RegistryMessageListener registryMessageListener = new RegistryMessageListener(actorRegistryListener);

        registryMessageListener.onMessage(message, closed(DUMMY_ACTOR));

        verify(actorRegistryListener).onActorClosed(DUMMY_ACTOR);
        verifyNoMoreInteractions(actorRegistryListener);
    }

    private Set<RegisteredActor> registeredActors() {
        Set<RegisteredActor> registeredActors = new HashSet<>();
        registeredActors.add(RegisteredActor.created(DUMMY_ACTOR, "agent"));
        return registeredActors;
    }
}