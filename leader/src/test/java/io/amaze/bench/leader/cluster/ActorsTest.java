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
package io.amaze.bench.leader.cluster;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.actor.*;
import io.amaze.bench.runtime.cluster.ActorSender;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryListener;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created on 9/16/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ActorsTest {

    private static final int FUTURE_TIMEOUT = 100;
    private static final String AGENT_NAME = "agent";
    private static final ActorKey ACTOR_KEY = new ActorKey("actor");
    private static final ActorKey OTHER_ACTOR = new ActorKey("other-actor");

    @Rule
    public Timeout globalTimeout = new Timeout(5, TimeUnit.SECONDS);
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private Endpoint endpoint;
    @Mock
    private ResourceManager resourceManager;
    @Mock
    private ActorRegistry actorRegistry;
    @Mock
    private ActorRegistryListener actorRegistryListener;
    @Mock
    private ActorSender actorSender;

    private Actors actors;
    private ActorConfig actorConfig;

    @Before
    public void initActors() {
        actors = new Actors(actorSender, resourceManager, actorRegistry);
        actorConfig = new ActorConfig(ACTOR_KEY, "className", new DeployConfig(false, new ArrayList<>()), "{}");

        doAnswer((invocationOnMock) -> actorRegistryListener = (ActorRegistryListener) invocationOnMock.getArguments()[0]).when(
                actorRegistry).addListener(any(ActorRegistryListener.class));
    }

    @Test
    public void null_parameters_are_invalid() {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorSender.class, actorSender);
        tester.setDefault(ResourceManager.class, resourceManager);

        tester.testAllPublicConstructors(Actors.class);
        tester.testAllPublicInstanceMethods(actors);
        tester.testAllPublicInstanceMethods(actorHandle);
    }

    @Test
    public void create_adds_listener_and_creates_actor() {

        Actors.ActorHandle actorHandle = actors.create(actorConfig);

        assertNotNull(actorHandle);
        InOrder inOrder = inOrder(actorRegistry, resourceManager);
        inOrder.verify(actorRegistry).addListener(actorRegistryListener);
        inOrder.verify(resourceManager).createActor(actorConfig);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void initialize_sends_message_to_actor() {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);

        Future<ActorDeployInfo> future = actorHandle.initialize();

        assertNotNull(future);
        verify(actorSender).sendToActor(ACTOR_KEY, ActorInputMessage.init());
        verifyNoMoreInteractions(actorSender);
    }

    @Test
    public void dumpMetrics_sends_message_to_actor() {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);

        actorHandle.dumpMetrics();

        verify(actorSender).sendToActor(ACTOR_KEY, ActorInputMessage.dumpMetrics());
        verifyNoMoreInteractions(actorSender);
    }

    @Test
    public void send_sends_message_to_actor() {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);
        String from = "from";
        String message = "message";

        actorHandle.send(from, message);

        verify(actorSender).sendToActor(ACTOR_KEY, ActorInputMessage.sendMessage(from, message));
        verifyNoMoreInteractions(actorSender);
    }

    @Test
    public void close_sends_message_to_actor() {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);

        Future<Void> close = actorHandle.close();

        assertNotNull(close);
        verify(actorSender).sendToActor(ACTOR_KEY, ActorInputMessage.close());
        verifyNoMoreInteractions(actorSender);
    }

    @Test
    public void actorCreation_is_set_when_actor_created() throws ExecutionException, InterruptedException {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);

        actorRegistryListener.onActorCreated(ACTOR_KEY, AGENT_NAME);

        ActorConfig actual = getUninterruptibly(actorHandle.actorCreation());
        assertSame(actual, actorConfig);
    }

    @Test(expected = TimeoutException.class)
    public void actorCreation_is_not_set_when_another_actor_created()
            throws ExecutionException, InterruptedException, TimeoutException {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);

        actorRegistryListener.onActorCreated(OTHER_ACTOR, AGENT_NAME);

        getUninterruptibly(actorHandle.actorCreation(), FUTURE_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Test
    public void actorInitialization_is_set_when_actor_initializes() throws ExecutionException, InterruptedException {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);

        ActorDeployInfo expected = new ActorDeployInfo(endpoint, 10);
        actorRegistryListener.onActorInitialized(ACTOR_KEY, expected);

        ActorDeployInfo actual = getUninterruptibly(actorHandle.actorInitialization());
        assertSame(actual, expected);
    }

    @Test(expected = TimeoutException.class)
    public void actorInitialization_is_not_set_when_another_actor_initializes()
            throws ExecutionException, InterruptedException, TimeoutException {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);

        ActorDeployInfo expected = new ActorDeployInfo(endpoint, 10);
        actorRegistryListener.onActorInitialized(OTHER_ACTOR, expected);

        getUninterruptibly(actorHandle.actorInitialization(), FUTURE_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Test
    public void actorTermination_is_set_when_actor_closes() throws ExecutionException, InterruptedException {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);

        actorRegistryListener.onActorClosed(ACTOR_KEY);

        Void actual = getUninterruptibly(actorHandle.actorTermination());
        assertNull(actual);
        InOrder inOrder = inOrder(actorRegistry);
        inOrder.verify(actorRegistry).addListener(actorRegistryListener);
        inOrder.verify(actorRegistry).removeListener(actorRegistryListener);
        inOrder.verifyNoMoreInteractions();
    }

    @Test(expected = TimeoutException.class)
    public void actorTermination_is_not_set_when_another_actor_closes()
            throws ExecutionException, InterruptedException, TimeoutException {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);

        actorRegistryListener.onActorClosed(OTHER_ACTOR);

        getUninterruptibly(actorHandle.actorTermination(), FUTURE_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Test
    public void actorFailure_is_set_when_actor_fails() throws ExecutionException, InterruptedException {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);

        RuntimeException expected = new RuntimeException();
        actorRegistryListener.onActorFailed(ACTOR_KEY, expected);

        Throwable actual = getUninterruptibly(actorHandle.actorFailure());
        assertSame(actual, expected);
        InOrder inOrder = inOrder(actorRegistry);
        inOrder.verify(actorRegistry).addListener(actorRegistryListener);
        inOrder.verify(actorRegistry).removeListener(actorRegistryListener);
        inOrder.verifyNoMoreInteractions();
    }

    @Test(expected = TimeoutException.class)
    public void actorFailure_is_not_set_when_another_actor_fails()
            throws ExecutionException, InterruptedException, TimeoutException {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);

        actorRegistryListener.onActorFailed(OTHER_ACTOR, new RuntimeException());

        getUninterruptibly(actorHandle.actorTermination(), FUTURE_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Test
    public void actorCreation_is_set_when_actor_fails()
            throws ExecutionException, InterruptedException, TimeoutException {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);

        verifyActorFailureThrowsFor(actorHandle.actorCreation());
    }

    @Test
    public void actorInitialization_throws_when_actor_fails()
            throws ExecutionException, InterruptedException, TimeoutException {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);

        verifyActorFailureThrowsFor(actorHandle.actorInitialization());
    }

    @Test
    public void actorTermination_throws_actor_fails()
            throws ExecutionException, InterruptedException, TimeoutException {
        Actors.ActorHandle actorHandle = actors.create(actorConfig);

        verifyActorFailureThrowsFor(actorHandle.actorTermination());
    }

    private void verifyActorFailureThrowsFor(final Future<?> future) throws ExecutionException {
        RuntimeException throwable = new RuntimeException();
        actorRegistryListener.onActorFailed(ACTOR_KEY, throwable);

        expectedException.expect(ExecutionException.class);
        expectedException.expectCause(is(throwable));

        getUninterruptibly(future);
    }

}