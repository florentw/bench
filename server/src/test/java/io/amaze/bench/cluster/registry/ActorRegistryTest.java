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
package io.amaze.bench.cluster.registry;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.client.runtime.actor.ActorDeployInfo;
import io.amaze.bench.cluster.registry.RegisteredActor.State;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.client.runtime.agent.AgentTest.DUMMY_AGENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created on 4/2/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ActorRegistryTest {

    private static final ActorDeployInfo DEPLOY_INFO = new ActorDeployInfo(10);

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

    @Test
    public void cluster_listener_is_a_logger() {
        assertTrue(registry.createClusterListener() instanceof ActorRegistryListenerLogger);
    }

    @Test
    public void actor_registered() {
        clusterListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        RegisteredActor actor = registry.byKey(DUMMY_ACTOR);

        assertThat(actor.getKey(), is(DUMMY_ACTOR));
        assertThat(actor.getAgent(), is(DUMMY_AGENT));
        assertThat(actor.getState(), is(State.CREATED));

        verify(clientListener).onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void list_all() {
        clusterListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);

        Set<RegisteredActor> actors = registry.all();
        assertThat(actors.size(), is(1));

        RegisteredActor actor = actors.iterator().next();
        assertThat(actor.getKey(), is(DUMMY_ACTOR));
        assertThat(actor.getState(), is(State.CREATED));
    }

    @Test
    public void actor_registered_and_initialized() {
        clusterListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        clusterListener.onActorInitialized(DUMMY_ACTOR, DEPLOY_INFO);

        RegisteredActor actor = registry.byKey(DUMMY_ACTOR);

        assertThat(actor.getKey(), is(DUMMY_ACTOR));
        assertThat(actor.getAgent(), is(DUMMY_AGENT));
        assertThat(actor.getState(), is(State.INITIALIZED));

        verify(clientListener).onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        verify(clientListener).onActorInitialized(DUMMY_ACTOR, DEPLOY_INFO);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void unknown_actor_initialized_does_not_throw() {
        clusterListener.onActorInitialized(DUMMY_ACTOR, DEPLOY_INFO);

        RegisteredActor actor = registry.byKey(DUMMY_ACTOR);
        assertNull(actor);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void actor_registered_and_closed_is_removed() {
        clusterListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        clusterListener.onActorClosed(DUMMY_ACTOR);

        RegisteredActor actor = registry.byKey(DUMMY_ACTOR);
        assertNull(actor);

        verify(clientListener).onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        verify(clientListener).onActorClosed(DUMMY_ACTOR);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void unknown_actor_closed_does_not_notify() {
        clusterListener.onActorClosed(DUMMY_ACTOR);

        RegisteredActor actor = registry.byKey(DUMMY_ACTOR);
        assertNull(actor);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void actor_registered_and_failed_is_removed() {
        Throwable dummyThrowable = new IllegalArgumentException();

        clusterListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        clusterListener.onActorFailed(DUMMY_ACTOR, dummyThrowable);

        RegisteredActor actor = registry.byKey(DUMMY_ACTOR);
        assertNull(actor);

        verify(clientListener).onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        verify(clientListener).onActorFailed(DUMMY_ACTOR, dummyThrowable);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void unknown_actor_failed_does_not_notify() {
        Throwable dummyThrowable = new IllegalArgumentException();

        clusterListener.onActorFailed(DUMMY_ACTOR, dummyThrowable);

        RegisteredActor actor = registry.byKey(DUMMY_ACTOR);
        assertNull(actor);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void removed_listener_not_notified() {
        registry.removeListener(clientListener);

        clusterListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void remove_listener_twice_does_not_throw() {
        registry.removeListener(clientListener);
        registry.removeListener(clientListener);

        clusterListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);

        verifyNoMoreInteractions(clientListener);
    }

}