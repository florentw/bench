package io.amaze.bench.orchestrator.registry;

import com.google.common.testing.NullPointerTester;
import org.junit.Before;
import org.junit.Test;

import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.client.runtime.agent.AgentTest.DUMMY_AGENT;
import static org.mockito.Mockito.*;

/**
 * Created on 3/29/16.
 */
public final class ActorRegistryListenerLoggerTest {

    private ActorRegistryListener delegateListener;
    private ActorRegistryListenerLogger loggerListener;

    @Before
    public void before() {
        delegateListener = mock(ActorRegistryListener.class);
        loggerListener = new ActorRegistryListenerLogger(delegateListener);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(ActorRegistryListenerLogger.class);
        tester.testAllPublicInstanceMethods(loggerListener);
    }

    @Test
    public void actor_created() {
        loggerListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);

        verify(delegateListener).onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        verifyNoMoreInteractions(delegateListener);
    }

    @Test
    public void actor_started() {
        loggerListener.onActorInitialized(DUMMY_ACTOR, DUMMY_AGENT);

        verify(delegateListener).onActorInitialized(DUMMY_ACTOR, DUMMY_AGENT);
        verifyNoMoreInteractions(delegateListener);
    }

    @Test
    public void actor_failed() throws Exception {
        IllegalArgumentException throwable = new IllegalArgumentException();
        loggerListener.onActorFailed(DUMMY_ACTOR, throwable);

        verify(delegateListener).onActorFailed(DUMMY_ACTOR, throwable);
        verifyNoMoreInteractions(delegateListener);
    }

    @Test
    public void actor_closed() throws Exception {
        loggerListener.onActorClosed(DUMMY_ACTOR);

        verify(delegateListener).onActorClosed(DUMMY_ACTOR);
        verifyNoMoreInteractions(delegateListener);
    }
}