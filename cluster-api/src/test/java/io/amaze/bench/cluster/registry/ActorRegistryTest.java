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
package io.amaze.bench.cluster.registry;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.Endpoint;
import io.amaze.bench.cluster.actor.ActorDeployInfo;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.agent.AgentUtil;
import io.amaze.bench.cluster.registry.RegisteredActor.State;
import io.amaze.bench.runtime.actor.TestActor;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created on 4/2/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ActorRegistryTest {

    private static final Endpoint ENDPOINT = new RegisteredActorTest.DummyEndpoint("key");
    private static final ActorDeployInfo DEPLOY_INFO = new ActorDeployInfo(ENDPOINT, 10);

    @Mock
    private Endpoint anotherEndpoint;
    @Mock
    private ActorRegistryListener clientListener;

    private ActorRegistry registry;
    private ActorRegistryListener clusterListener;

    @Before
    public void before() {
        registry = new ActorRegistry();
        clusterListener = registry.createClusterListener();

        registry.addListener(clientListener);
    }

    @After
    public void after() {
        registry.removeListener(clientListener);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(ActorRegistry.class);
        tester.testAllPublicInstanceMethods(registry);
    }

    @Test(expected = IllegalStateException.class)
    public void adding_same_listener_a_second_time_throws() {
        registry.addListener(clientListener);
    }

    @Test
    public void cluster_listener_is_a_logger() {
        assertTrue(registry.createClusterListener() instanceof ActorRegistryListenerLogger);
    }

    @Test
    public void actor_registered() {
        clusterListener.onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);
        RegisteredActor actor = registry.byKey(TestActor.DUMMY_ACTOR);

        assertThat(actor.getKey(), Is.is(TestActor.DUMMY_ACTOR));
        assertThat(actor.getAgent(), Is.is(AgentUtil.DUMMY_AGENT));
        assertThat(actor.getState(), is(State.CREATED));

        verify(clientListener).onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void actor_created_twice_is_registered_only_once() {
        clusterListener.onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);
        clusterListener.onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);

        RegisteredActor actor = registry.byKey(TestActor.DUMMY_ACTOR);

        assertThat(actor.getKey(), Is.is(TestActor.DUMMY_ACTOR));
        assertThat(actor.getAgent(), Is.is(AgentUtil.DUMMY_AGENT));
        assertThat(actor.getState(), is(State.CREATED));

        verify(clientListener).onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void list_all() {
        clusterListener.onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);

        Set<RegisteredActor> actors = registry.all();
        assertThat(actors.size(), is(1));

        RegisteredActor actor = actors.iterator().next();
        assertThat(actor.getKey(), Is.is(TestActor.DUMMY_ACTOR));
        assertThat(actor.getState(), is(State.CREATED));
    }

    @Test
    public void resetState_overrides_current_view() {
        clusterListener.onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);
        Set<RegisteredActor> newActorSet = new HashSet<>();
        RegisteredActor newActor = RegisteredActor.created(new ActorKey("dummy-new"), new AgentKey("agent"));
        newActorSet.add(newActor);

        registry.resetState(newActorSet);

        assertThat(registry.all(), is(newActorSet));
    }

    @Test
    public void actor_registered_and_initialized() {
        clusterListener.onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);
        clusterListener.onActorInitialized(TestActor.DUMMY_ACTOR, DEPLOY_INFO);

        RegisteredActor actor = registry.byKey(TestActor.DUMMY_ACTOR);

        assertThat(actor.getKey(), Is.is(TestActor.DUMMY_ACTOR));
        assertThat(actor.getAgent(), Is.is(AgentUtil.DUMMY_AGENT));
        assertThat(actor.getState(), is(State.INITIALIZED));

        verify(clientListener).onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);
        verify(clientListener).onActorInitialized(TestActor.DUMMY_ACTOR, DEPLOY_INFO);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void unknown_actor_initialized_does_not_throw() {
        clusterListener.onActorInitialized(TestActor.DUMMY_ACTOR, DEPLOY_INFO);

        RegisteredActor actor = registry.byKey(TestActor.DUMMY_ACTOR);
        assertNull(actor);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void actor_registered_and_closed_is_removed() {
        clusterListener.onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);
        clusterListener.onActorClosed(TestActor.DUMMY_ACTOR);

        RegisteredActor actor = registry.byKey(TestActor.DUMMY_ACTOR);
        assertNull(actor);

        verify(clientListener).onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);
        verify(clientListener).onActorClosed(TestActor.DUMMY_ACTOR);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void unknown_actor_closed_does_not_notify() {
        clusterListener.onActorClosed(TestActor.DUMMY_ACTOR);

        RegisteredActor actor = registry.byKey(TestActor.DUMMY_ACTOR);
        assertNull(actor);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void actor_registered_and_failed_is_removed() {
        Throwable dummyThrowable = new IllegalArgumentException();

        clusterListener.onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);
        clusterListener.onActorFailed(TestActor.DUMMY_ACTOR, dummyThrowable);

        RegisteredActor actor = registry.byKey(TestActor.DUMMY_ACTOR);
        assertNull(actor);

        verify(clientListener).onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);
        verify(clientListener).onActorFailed(TestActor.DUMMY_ACTOR, dummyThrowable);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void unknown_actor_failed_does_not_notify() {
        Throwable dummyThrowable = new IllegalArgumentException();

        clusterListener.onActorFailed(TestActor.DUMMY_ACTOR, dummyThrowable);

        RegisteredActor actor = registry.byKey(TestActor.DUMMY_ACTOR);
        assertNull(actor);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void removed_listener_not_notified() {
        registry.removeListener(clientListener);

        clusterListener.onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void remove_listener_twice_does_not_throw() {
        registry.removeListener(clientListener);
        registry.removeListener(clientListener);

        clusterListener.onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void actor_is_removed_when_endpoint_disconnects_and_listeners_are_notified() {
        clusterListener.onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);
        clusterListener.onActorInitialized(TestActor.DUMMY_ACTOR, DEPLOY_INFO);

        registry.onEndpointDisconnected(ENDPOINT);

        assertNull(registry.byKey(TestActor.DUMMY_ACTOR));
        verify(clientListener).onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);
        verify(clientListener).onActorInitialized(TestActor.DUMMY_ACTOR, DEPLOY_INFO);
        verify(clientListener).onActorFailed(eq(TestActor.DUMMY_ACTOR), any(ActorDisconnectedException.class));
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void actor_is_not_removed_when_another_endpoint_disconnects() {
        clusterListener.onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);

        registry.onEndpointDisconnected(anotherEndpoint);

        assertNotNull(registry.byKey(TestActor.DUMMY_ACTOR));
        verify(clientListener).onActorCreated(TestActor.DUMMY_ACTOR, AgentUtil.DUMMY_AGENT);
        verifyNoMoreInteractions(clientListener);
    }

}