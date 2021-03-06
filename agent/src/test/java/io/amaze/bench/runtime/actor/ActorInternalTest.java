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
package io.amaze.bench.runtime.actor;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.api.ActorKey;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import io.amaze.bench.cluster.Endpoint;
import io.amaze.bench.cluster.actor.*;
import io.amaze.bench.cluster.metric.MetricValue;
import io.amaze.bench.cluster.metric.MetricValuesMessage;
import io.amaze.bench.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.runtime.actor.metric.MetricsInternal;
import io.amaze.bench.runtime.agent.DummyClientFactory;
import io.amaze.bench.shared.test.Json;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static io.amaze.bench.cluster.actor.ActorLifecycleMessage.State;
import static io.amaze.bench.runtime.actor.TestActor.*;
import static io.amaze.bench.runtime.actor.TestActorMetrics.DUMMY_METRIC_A;
import static io.amaze.bench.runtime.actor.TestActorMetrics.DUMMY_METRIC_B;
import static io.amaze.bench.util.Matchers.isActorState;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created on 3/13/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ActorInternalTest {

    private DummyClientFactory clientFactory;
    private Actors factory;

    @Mock
    private ActorClusterClient actorClient;
    @Mock
    private ActorRegistryClusterClient actorRegistryClient;
    @Mock
    private ActorSender actorSender;
    @Mock
    private ActorRegistrySender actorRegistrySender;
    @Mock
    private Endpoint localEndpoint;

    @Before
    public void before() {
        doReturn(localEndpoint).when(actorClient).localEndpoint();
        doReturn(actorSender).when(actorClient).actorSender();
        doReturn(actorRegistrySender).when(actorClient).actorRegistrySender();
        clientFactory = new DummyClientFactory(localEndpoint, null, actorClient, actorRegistryClient, null, null);
        factory = new Actors(clientFactory);
    }

    @Test
    public void null_parameters_are_invalid() throws ValidationException {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(Metrics.class, MetricsInternal.create(TestActor.DUMMY_ACTOR));
        tester.setDefault(Method.class, ActorInternalTest.class.getMethods()[0]);
        try (ActorInternal actor = defaultTestActor()) {
            tester.testAllPublicInstanceMethods(actor);
        }
    }

    @Test
    public void toString_yields_valid_json() throws ValidationException {
        try (ActorInternal actor = defaultTestActor()) {
            assertTrue(Json.isValid(actor.toString()));
        }
    }

    @Test
    public void create_init_actor() throws Exception {
        try (ActorInternal actor = defaultTestActor()) {

            actor.init();

            // Check before method is called
            assertThat(actor.getKey(), is(DUMMY_ACTOR));
            assertThat(((TestActor) actor.getInstance()).isBeforeCalled(), is(true));

            verify(actorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.INITIALIZED)));
        }
    }

    @Test(expected = ValidationException.class)
    public void create_invalid_actor_throws() throws ValidationException {
        factory.create(DUMMY_ACTOR, String.class.getName(), DUMMY_JSON_CONFIG);
    }

    @Test(expected = ValidationException.class)
    public void create_actor_with_invalid_config_throws() throws ValidationException {
        factory.create(DUMMY_ACTOR, TestActor.class.getName(), "@#$%-TEST");
    }

    @Test
    public void initialize_actor_throws() throws Exception {
        try (ActorInternal actor = createActor(TestActorBeforeThrows.class)) {

            actor.init();

            // Assert after method was called
            assertTrue(((TestActor) actor.getInstance()).isAfterCalled());

            verify(actorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.FAILED)));
            verifyNoMoreInteractions(actorRegistrySender);
        }
    }

    @Test
    public void initialize_actor_failure_and_call_to_after_throws() throws Exception {
        try (ActorInternal actor = createActor(TestActorBeforeAndAfterThrows.class)) {

            actor.init();

            verify(actorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.FAILED)));
            verifyNoMoreInteractions(actorRegistrySender);
        }
    }

    @Test
    public void initialize_actor_no_before_method() throws Exception {
        Class<?> actorClass = TestActorNoBeforeNoAfter.class;
        try (ActorInternal actor = createActor(actorClass)) {

            actor.init();

            verify(actorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.INITIALIZED)));
        }
    }

    @Test
    public void bootstrap_on_reactor_with_no_method_sends_failure_message() throws ValidationException {
        try (ActorInternal actor = defaultTestActor()) {

            actor.bootstrap();

            verify(actorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.FAILED)));
            verifyNoMoreInteractions(actorRegistrySender);
            // Assert after method was called
            assertTrue(((TestActor) actor.getInstance()).isAfterCalled());
        }
    }

    @Test
    public void bootstrap_on_reactor_with_method_that_throws_sends_failure_message() throws ValidationException {
        try (ActorInternal actor = createActor(TestActorBootstrapThrows.class)) {

            actor.bootstrap();

            verify(actorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.FAILED)));
            verifyNoMoreInteractions(actorRegistrySender);
            // Assert after method was called
            assertTrue(((TestActorBootstrapThrows) actor.getInstance()).isAfterCalled());
        }
    }

    @Test
    public void bootstrap_on_reactor_with_method_that_throws_then_after_throws() throws ValidationException {
        try (ActorInternal actor = createActor(TestActorBootstrapThrows.class)) {
            ((TestActorBootstrapThrows) actor.getInstance()).setAfterMethodThrows(true);

            actor.bootstrap();

            verify(actorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.FAILED)));
            verifyNoMoreInteractions(actorRegistrySender);
            // Assert after method was called
            assertTrue(((TestActorBootstrapThrows) actor.getInstance()).isAfterCalled());
        }
    }

    @Test
    public void bootstrap_on_reactor_with_valid_method_calls_the_method() throws ValidationException {
        try (ActorInternal actor = createActor(TestActorBootstrap.class)) {

            actor.bootstrap();

            // Assert bootstrap method was called
            assertTrue(((TestActorBootstrap) actor.getInstance()).isBootstrapCalled());
            verifyNoMoreInteractions(actorRegistrySender);
        }
    }

    @Test
    public void actor_receives_message() throws Exception {
        try (ActorInternal actor = defaultTestActor()) {

            actor.onMessage(DUMMY_ACTOR.getName(), "");

            // Assert message is received by the instance
            assertThat(((TestActor) actor.getInstance()).awaitFirstAndReturnMessages().size(), is(1));
        }
    }

    @Test
    public void actor_sends_message() throws Exception {
        try (ActorInternal actor = defaultTestActor()) {
            TestActor reactor = (TestActor) actor.getInstance();
            String to = "hello";
            Serializable payload = "world";
            reactor.getSender().send(to, payload);

            verify(actorSender).send(eq(new ActorKey(to)), argThat(isMessage(payload)));
            verifyNoMoreInteractions(actorSender);
        }
    }

    @Test
    public void recoverableException_is_swallowed_and_actor_not_closed() throws Exception {
        try (ActorInternal actor = defaultTestActor()) {

            actor.onMessage(DUMMY_ACTOR.getName(), RECOVERABLE_EXCEPTION_MSG);

            assertFalse(((TestActor) actor.getInstance()).isAfterCalled());
            verifyZeroInteractions(actorClient);
        }
    }

    @Test
    public void actor_throws_IrrecoverableException_on_received_message() throws Exception {
        verifyIrrecoverableExceptions(FAIL_MSG);
    }

    @Test
    public void actor_throws_RuntimeException_on_received_message() throws Exception {
        verifyIrrecoverableExceptions(RUNTIME_EXCEPTION_MSG);
    }

    @Test
    public void actor_receives_message_and_throws_termination_exception() throws Exception {
        try (ActorInternal actor = defaultTestActor()) {

            actor.onMessage(DUMMY_ACTOR.getName(), TERMINATE_MSG);

            // Assert after method was called
            assertTrue(((TestActor) actor.getInstance()).isAfterCalled());

            InOrder inOrder = inOrder(actorRegistrySender, actorClient);
            inOrder.verify(actorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.CLOSED)));
            inOrder.verify(actorClient).close();
            inOrder.verifyNoMoreInteractions();
        }
    }

    @Test
    public void actor_throws_on_received_message_and_call_to_after_throws() throws Exception {
        try (ActorInternal actor = createActor(TestActorAfterThrows.class)) {

            actor.onMessage(DUMMY_ACTOR.getName(), FAIL_MSG);

            verify(actorRegistrySender).send(argThat(isActorState(State.FAILED)));
            verifyNoMoreInteractions(actorRegistrySender);
        }
    }

    @Test
    public void dump_metrics_throws_and_failure_message_is_sent() throws Exception {
        try (ActorInternal actor = createActor(TestActorMetrics.class)) {
            actor.onMessage(DUMMY_ACTOR.getName(), TestActorMetrics.PRODUCE_METRICS_MSG);

            doThrow(new IllegalArgumentException()).when(actorClient).sendMetrics(any(MetricValuesMessage.class));
            actor.dumpAndFlushMetrics();

            InOrder inOrder = inOrder(actorClient, actorRegistrySender);
            inOrder.verify(actorClient).sendMetrics(any(MetricValuesMessage.class));
            inOrder.verify(actorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.FAILED)));
            inOrder.verifyNoMoreInteractions();
        }
    }

    @Test
    public void create_dump_metrics_sends_message_to_metrics_actor() throws Exception {
        try (ActorInternal actor = createActor(TestActorMetrics.class)) {
            actor.init();

            actor.onMessage(DUMMY_ACTOR.getName(), TestActorMetrics.PRODUCE_METRICS_MSG);

            actor.dumpAndFlushMetrics();

            ArgumentMatcher<MetricValuesMessage> matchesMetrics = new ArgumentMatcher<MetricValuesMessage>() {
                @Override
                public boolean matches(final Object argument) {
                    MetricValuesMessage msg = (MetricValuesMessage) argument;
                    Map<Metric, List<MetricValue>> metricsMap = msg.metrics();
                    assertThat(metricsMap.size(), is(2));
                    assertThat(metricsMap.get(DUMMY_METRIC_A).size(), is(1));
                    assertThat(metricsMap.get(DUMMY_METRIC_A).get(0).getValue(), is(10));
                    assertThat(metricsMap.get(DUMMY_METRIC_B).size(), is(1));
                    assertThat(metricsMap.get(DUMMY_METRIC_B).get(0).getValue(), is(1));
                    return true;
                }
            };
            InOrder inOrder = inOrder(actorRegistrySender, actorClient);
            inOrder.verify(actorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.INITIALIZED)));
            inOrder.verify(actorClient).sendMetrics(argThat(matchesMetrics));
            inOrder.verifyNoMoreInteractions();
        }
    }

    @Test
    public void close_actor_and_closing_client_throws() throws Exception {
        clientFactory = new DummyClientFactory(null, null, actorClient, actorRegistryClient, null, null);
        factory = new Actors(clientFactory);
        doThrow(new RuntimeException()).when(actorClient).close();
        ActorInternal actor = defaultTestActor();

        actor.close();

        InOrder inOrder = inOrder(actorRegistrySender, actorClient);
        inOrder.verify(actorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.CLOSED)));
        inOrder.verify(actorClient).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void close_actor_calls_after() throws Exception {
        ActorInternal actor = defaultTestActor();

        actor.close();

        assertTrue(((TestActor) actor.getInstance()).isAfterCalled());
        InOrder inOrder = inOrder(actorRegistrySender, actorClient);
        inOrder.verify(actorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.CLOSED)));
        inOrder.verify(actorClient).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void close_actor_twice_closes_once() throws Exception {
        ActorInternal actor = defaultTestActor();

        actor.close();
        actor.close();

        assertTrue(((TestActor) actor.getInstance()).isAfterCalled());
        InOrder inOrder = inOrder(actorRegistrySender, actorClient);
        inOrder.verify(actorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.CLOSED)));
        inOrder.verify(actorClient).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void close_actor_and_after_method_throws() throws Exception {
        ActorInternal actor = createActor(TestActorAfterThrows.class);
        actor.close();

        InOrder inOrder = inOrder(actorRegistrySender, actorClient);
        inOrder.verify(actorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.FAILED)));
        inOrder.verify(actorClient).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void close_actor_and_sending_lifecycle_msg_throws() throws Exception {
        ActorInternal actor = defaultTestActor();

        doThrow(new IllegalArgumentException()).when(actorRegistrySender).send(any(ActorLifecycleMessage.class));

        actor.close();

        InOrder inOrder = inOrder(actorRegistrySender, actorClient);
        inOrder.verify(actorRegistrySender).send(argThat(isActorState(ActorLifecycleMessage.State.CLOSED)));
        inOrder.verify(actorClient).close();
        inOrder.verifyNoMoreInteractions();
    }

    private ArgumentMatcher<ActorInputMessage> isMessage(final Serializable payload) {
        return new ArgumentMatcher<ActorInputMessage>() {
            @Override
            public boolean matches(final Object argument) {
                ActorInputMessage msg = (ActorInputMessage) argument;
                return msg.getPayload().equals(payload);
            }
        };
    }

    private void verifyIrrecoverableExceptions(String messageToActor) throws ValidationException {
        try (ActorInternal actor = defaultTestActor()) {

            actor.onMessage(DUMMY_ACTOR.getName(), messageToActor);

            // Assert after method was called
            assertTrue(((TestActor) actor.getInstance()).isAfterCalled());
            verify(actorRegistrySender).send(argThat(isActorState(State.FAILED)));
            verifyNoMoreInteractions(actorRegistrySender);
        }
    }

    private ActorInternal createActor(final Class<?> actorClass) throws ValidationException {
        ActorInternal actorInternal = (ActorInternal) factory.create(DUMMY_ACTOR,
                                                                     actorClass.getName(),
                                                                     DUMMY_JSON_CONFIG);
        verify(actorClient).startActorListener(actorInternal);
        return actorInternal;
    }

    private ActorInternal defaultTestActor() throws ValidationException {
        return createActor(TestActor.class);
    }
}