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
package io.amaze.bench.orchestrator.registry;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.orchestrator.registry.RegisteredActor.State;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.client.runtime.agent.AgentTest.DUMMY_AGENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created on 4/2/16.
 */
public final class ActorRegistryTest {

    private ActorRegistry registry;
    private ActorRegistryListener orchestratorListener;
    private ActorRegistryListener clientListener;

    @Before
    public void before() {
        registry = new ActorRegistry();
        orchestratorListener = registry.getListenerForOrchestrator();

        clientListener = mock(ActorRegistryListener.class);
        registry.addListener(clientListener);
    }

    @After
    public void after() {
        registry.removeListener(clientListener);
    }

    @Test
    public void listener_for_orchestrator_is_a_logger() {
        assertTrue(registry.getListenerForOrchestrator() instanceof ActorRegistryListenerLogger);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(ActorRegistry.class);
        tester.testAllPublicInstanceMethods(registry);
    }

    @Test
    public void actor_registered() {
        orchestratorListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        RegisteredActor actor = registry.byName(DUMMY_ACTOR);

        assertThat(actor.getName(), is(DUMMY_ACTOR));
        assertThat(actor.getAgent(), is(DUMMY_AGENT));
        assertThat(actor.getState(), is(State.CREATED));

        verify(clientListener).onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void list_all() {
        orchestratorListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);

        Set<RegisteredActor> actors = registry.all();
        assertThat(actors.size(), is(1));

        RegisteredActor actor = actors.iterator().next();
        assertThat(actor.getName(), is(DUMMY_ACTOR));
        assertThat(actor.getState(), is(State.CREATED));
    }

    @Test
    public void actor_registered_and_initialized() {
        orchestratorListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        orchestratorListener.onActorInitialized(DUMMY_ACTOR, DUMMY_AGENT);

        RegisteredActor actor = registry.byName(DUMMY_ACTOR);

        assertThat(actor.getName(), is(DUMMY_ACTOR));
        assertThat(actor.getAgent(), is(DUMMY_AGENT));
        assertThat(actor.getState(), is(State.INITIALIZED));

        verify(clientListener).onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        verify(clientListener).onActorInitialized(DUMMY_ACTOR, DUMMY_AGENT);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void unknown_actor_initialized_does_not_throw() {
        orchestratorListener.onActorInitialized(DUMMY_ACTOR, DUMMY_AGENT);

        RegisteredActor actor = registry.byName(DUMMY_ACTOR);
        assertNull(actor);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void actor_registered_and_closed_is_removed() {
        orchestratorListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        orchestratorListener.onActorClosed(DUMMY_ACTOR);

        RegisteredActor actor = registry.byName(DUMMY_ACTOR);
        assertNull(actor);

        verify(clientListener).onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        verify(clientListener).onActorClosed(DUMMY_ACTOR);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void unknown_actor_closed_does_not_notify() {
        orchestratorListener.onActorClosed(DUMMY_ACTOR);

        RegisteredActor actor = registry.byName(DUMMY_ACTOR);
        assertNull(actor);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void actor_registered_and_failed_is_removed() {
        Throwable dummyThrowable = new IllegalArgumentException();

        orchestratorListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        orchestratorListener.onActorFailed(DUMMY_ACTOR, dummyThrowable);

        RegisteredActor actor = registry.byName(DUMMY_ACTOR);
        assertNull(actor);

        verify(clientListener).onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        verify(clientListener).onActorFailed(DUMMY_ACTOR, dummyThrowable);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void unknown_actor_failed_does_not_notify() {
        Throwable dummyThrowable = new IllegalArgumentException();

        orchestratorListener.onActorFailed(DUMMY_ACTOR, dummyThrowable);

        RegisteredActor actor = registry.byName(DUMMY_ACTOR);
        assertNull(actor);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void removed_listener_not_notified() {
        registry.removeListener(clientListener);

        orchestratorListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void remove_listener_twice_does_not_throw() {
        registry.removeListener(clientListener);
        registry.removeListener(clientListener);

        orchestratorListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);

        verifyNoMoreInteractions(clientListener);
    }

}