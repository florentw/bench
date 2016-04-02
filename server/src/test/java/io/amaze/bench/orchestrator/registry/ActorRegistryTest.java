package io.amaze.bench.orchestrator.registry;

import io.amaze.bench.TestConstants;
import io.amaze.bench.orchestrator.registry.RegisteredActor.State;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created on 4/2/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
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
    public void actor_registered() {
        orchestratorListener.onActorCreated(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);
        RegisteredActor actor = registry.byName(TestConstants.DUMMY_ACTOR);

        assertThat(actor.getName(), is(TestConstants.DUMMY_ACTOR));
        assertThat(actor.getAgent(), is(TestConstants.DUMMY_AGENT));
        assertThat(actor.getState(), is(State.CREATED));

        verify(clientListener).onActorCreated(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void list_all() {
        orchestratorListener.onActorCreated(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);

        Set<RegisteredActor> actors = registry.all();
        assertThat(actors.size(), is(1));

        RegisteredActor actor = actors.iterator().next();
        assertThat(actor.getName(), is(TestConstants.DUMMY_ACTOR));
        assertThat(actor.getState(), is(State.CREATED));
    }

    @Test
    public void actor_registered_and_started() {
        orchestratorListener.onActorCreated(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);
        orchestratorListener.onActorStarted(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);

        RegisteredActor actor = registry.byName(TestConstants.DUMMY_ACTOR);

        assertThat(actor.getName(), is(TestConstants.DUMMY_ACTOR));
        assertThat(actor.getAgent(), is(TestConstants.DUMMY_AGENT));
        assertThat(actor.getState(), is(State.STARTED));

        verify(clientListener).onActorCreated(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);
        verify(clientListener).onActorStarted(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void unknown_actor_started_does_not_throw() {
        orchestratorListener.onActorStarted(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);

        RegisteredActor actor = registry.byName(TestConstants.DUMMY_ACTOR);
        assertNull(actor);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void actor_registered_and_closed_is_removed() {
        orchestratorListener.onActorCreated(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);
        orchestratorListener.onActorClosed(TestConstants.DUMMY_ACTOR);

        RegisteredActor actor = registry.byName(TestConstants.DUMMY_ACTOR);
        assertNull(actor);

        verify(clientListener).onActorCreated(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);
        verify(clientListener).onActorClosed(TestConstants.DUMMY_ACTOR);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void unknown_actor_closed_does_not_notify() {
        orchestratorListener.onActorClosed(TestConstants.DUMMY_ACTOR);

        RegisteredActor actor = registry.byName(TestConstants.DUMMY_ACTOR);
        assertNull(actor);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void actor_registered_and_failed_is_removed() {
        Throwable dummyThrowable = new IllegalArgumentException();

        orchestratorListener.onActorCreated(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);
        orchestratorListener.onActorFailed(TestConstants.DUMMY_ACTOR, dummyThrowable);

        RegisteredActor actor = registry.byName(TestConstants.DUMMY_ACTOR);
        assertNull(actor);

        verify(clientListener).onActorCreated(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);
        verify(clientListener).onActorFailed(TestConstants.DUMMY_ACTOR, dummyThrowable);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void unknown_actor_failed_does_not_notify() {
        Throwable dummyThrowable = new IllegalArgumentException();

        orchestratorListener.onActorFailed(TestConstants.DUMMY_ACTOR, dummyThrowable);

        RegisteredActor actor = registry.byName(TestConstants.DUMMY_ACTOR);
        assertNull(actor);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void removed_listener_not_notified() {
        registry.removeListener(clientListener);

        orchestratorListener.onActorCreated(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void remove_listener_twice_does_not_throw() {
        registry.removeListener(clientListener);
        registry.removeListener(clientListener);

        orchestratorListener.onActorCreated(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);

        verifyNoMoreInteractions(clientListener);
    }

}