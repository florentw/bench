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
package io.amaze.bench.client.runtime.actor;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import io.amaze.bench.client.runtime.actor.metric.MetricValue;
import io.amaze.bench.client.runtime.actor.metric.MetricsInternal;
import io.amaze.bench.client.runtime.agent.AgentOutputMessage;
import io.amaze.bench.client.runtime.agent.Constants;
import io.amaze.bench.client.runtime.agent.DummyClientFactory;
import io.amaze.bench.client.runtime.agent.RecorderOrchestratorActor;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.shared.test.Json;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static io.amaze.bench.client.runtime.actor.TestActor.*;
import static io.amaze.bench.client.runtime.actor.TestActorMetrics.DUMMY_METRIC_A;
import static io.amaze.bench.client.runtime.actor.TestActorMetrics.DUMMY_METRIC_B;
import static io.amaze.bench.client.runtime.agent.AgentTest.DUMMY_AGENT;
import static io.amaze.bench.client.runtime.agent.Constants.METRICS_ACTOR_NAME;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created on 3/13/16.
 */
public final class BaseActorTest {

    private RecorderOrchestratorActor actorClient;
    private DummyClientFactory clientFactory;
    private Actors factory;

    @Before
    public void before() {
        actorClient = spy(new RecorderOrchestratorActor());
        clientFactory = new DummyClientFactory(null, actorClient);
        factory = new Actors(DUMMY_AGENT, clientFactory);
    }

    @Test
    public void null_parameters_are_invalid() throws ValidationException {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(Metrics.class, MetricsInternal.create());
        tester.setDefault(Method.class, BaseActorTest.class.getMethods()[0]);
        try (BaseActor actor = defaultTestActor()) {
            tester.testAllPublicInstanceMethods(actor);
        }
    }

    @Test
    public void toString_yields_valid_json() throws ValidationException {
        try (BaseActor actor = defaultTestActor()) {
            assertTrue(Json.isValid(actor.toString()));
        }
    }

    @Test
    public void create_init_actor() throws Exception {
        try (BaseActor actor = defaultTestActor()) {

            actor.init();

            // Check before method is called
            assertThat(actor.name(), is(DUMMY_ACTOR));
            assertThat(((TestActor) actor.getInstance()).isBeforeCalled(), is(true));

            // Check good flow of messages
            assertThat(actorClient.getSentMessages().size(), is(1));
            assertThat(messagesSentToMasterFrom(actorClient).size(), is(1));

            // Check initialized message sent
            ActorLifecycleMessage lfMsg = firstMessageToMaster(messagesSentToMasterFrom(actorClient));
            assertThat(lfMsg.getPhase(), is(ActorLifecycleMessage.Phase.INITIALIZED));
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
        try (BaseActor actor = createActor(TestActorBeforeThrows.class)) {

            actor.init();

            // Assert after method was called
            assertTrue(((TestActor) actor.getInstance()).isAfterCalled());

            // Check good flow of messages
            assertThat(actorClient.getSentMessages().size(), is(1));
            assertThat(messagesSentToMasterFrom(actorClient).size(), is(1));

            // Check failed message sent
            ActorLifecycleMessage lfMsg = firstMessageToMaster(messagesSentToMasterFrom(actorClient));
            assertThat(lfMsg.getPhase(), is(ActorLifecycleMessage.Phase.FAILED));
        }
    }

    @Test
    public void initialize_actor_failure_and_call_to_after_throws() throws Exception {
        try (BaseActor actor = createActor(TestActorBeforeAndAfterThrows.class)) {

            actor.init();

            // Check good flow of messages
            assertThat(actorClient.getSentMessages().size(), is(1));
            assertThat(messagesSentToMasterFrom(actorClient).size(), is(1));

            // Check failed message sent
            ActorLifecycleMessage lfMsg = firstMessageToMaster(messagesSentToMasterFrom(actorClient));
            assertThat(lfMsg.getPhase(), is(ActorLifecycleMessage.Phase.FAILED));
        }
    }

    @Test
    public void initialize_actor_no_before_method() throws Exception {
        Class<?> actorClass = TestActorNoBeforeNoAfter.class;
        try (BaseActor actor = createActor(actorClass)) {

            actor.init();

            // Check good flow of messages
            assertThat(actorClient.getSentMessages().size(), is(1));
            assertThat(messagesSentToMasterFrom(actorClient).size(), is(1));

            // Check initialized message sent
            ActorLifecycleMessage lfMsg = firstMessageToMaster(messagesSentToMasterFrom(actorClient));
            assertThat(lfMsg.getPhase(), is(ActorLifecycleMessage.Phase.INITIALIZED));
        }
    }

    @Test
    public void actor_receives_message() throws Exception {
        try (BaseActor actor = defaultTestActor()) {

            actor.onMessage(DUMMY_ACTOR, "");

            // Assert message is received by the instance
            assertThat(((TestActor) actor.getInstance()).getReceivedMessages().size(), is(1));
        }
    }

    @Test
    public void actor_sends_message() throws Exception {
        try (BaseActor actor = defaultTestActor()) {
            TestActor reactor = (TestActor) actor.getInstance();
            reactor.getSender().send("hello", "world");

            // Check message was sent
            assertThat(actorClient.getSentMessages().size(), is(1));
            List<Message<? extends Serializable>> msgToActor = actorClient.getSentMessages().get("hello");
            assertThat(msgToActor.size(), is(1));
            assertThat(msgToActor.get(0).data(), is("world"));
        }
    }

    @Test
    public void recoverableException_is_swallowed_and_actor_not_closed() throws Exception {
        try (BaseActor actor = defaultTestActor()) {

            actor.onMessage(DUMMY_ACTOR, RECOVERABLE_EXCEPTION_MSG);

            assertFalse(((TestActor) actor.getInstance()).isAfterCalled());
            // Check good no message sent
            assertTrue(actorClient.getSentMessages().isEmpty());
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
        try (BaseActor actor = defaultTestActor()) {

            actor.onMessage(DUMMY_ACTOR, TERMINATE_MSG);

            // Assert after method was called
            assertTrue(((TestActor) actor.getInstance()).isAfterCalled());

            // Check good flow of messages
            assertThat(actorClient.getSentMessages().size(), is(1));
            assertThat(messagesSentToMasterFrom(actorClient).size(), is(1));

            // Check closed message sent
            ActorLifecycleMessage lfMsg = firstMessageToMaster(messagesSentToMasterFrom(actorClient));
            assertThat(lfMsg.getPhase(), is(ActorLifecycleMessage.Phase.CLOSED));
        }
    }

    @Test
    public void actor_throws_on_received_message_and_call_to_after_throws() throws Exception {
        try (BaseActor actor = createActor(TestActorAfterThrows.class)) {

            actor.onMessage(DUMMY_ACTOR, FAIL_MSG);

            // Check good flow of messages
            assertThat(actorClient.getSentMessages().size(), is(1));
            assertThat(messagesSentToMasterFrom(actorClient).size(), is(1));

            // Check failed message sent
            ActorLifecycleMessage lfMsg = firstMessageToMaster(messagesSentToMasterFrom(actorClient));
            assertThat(lfMsg.getPhase(), is(ActorLifecycleMessage.Phase.FAILED));
        }
    }

    @Test
    public void create_dump_metrics_sends_message_to_metrics_actor() throws Exception {
        try (BaseActor actor = createActor(TestActorMetrics.class)) {
            actor.init();

            actor.onMessage(DUMMY_ACTOR, TestActorMetrics.PRODUCE_METRICS_MSG);

            actor.dumpAndFlushMetrics();

            // Check good flow of messages
            assertThat(actorClient.getSentMessages().size(), is(2));
            List<Message<? extends Serializable>> msgsToMetrics = actorClient.getSentMessages().get(Constants.METRICS_ACTOR_NAME);
            assertThat(msgsToMetrics.size(), is(1));

            // Check dump message sent
            Map<Metric, List<MetricValue>> metricsMap = (Map<Metric, List<MetricValue>>) msgsToMetrics.get(0).data(); // NOSONAR
            assertThat(metricsMap.size(), is(2));
            assertThat(metricsMap.get(DUMMY_METRIC_A).size(), is(1));
            assertThat(metricsMap.get(DUMMY_METRIC_A).get(0).getValue(), is(10));
            assertThat(metricsMap.get(DUMMY_METRIC_B).size(), is(1));
            assertThat(metricsMap.get(DUMMY_METRIC_B).get(0).getValue(), is(1));
        }
    }

    @Test
    public void dump_metrics_throws_and_failure_message_is_sent() throws Exception {
        try (BaseActor actor = createActor(TestActorMetrics.class)) {
            actor.onMessage(DUMMY_ACTOR, TestActorMetrics.PRODUCE_METRICS_MSG);

            doThrow(new IllegalArgumentException()).when(actorClient).sendToActor(eq(METRICS_ACTOR_NAME),
                                                                                  any(Message.class));
            actor.dumpAndFlushMetrics();

            // Check good flow of messages
            assertThat(actorClient.getSentMessages().size(), is(1));
            assertThat(messagesSentToMasterFrom(actorClient).size(), is(1));

            // Check failed message sent
            ActorLifecycleMessage lfMsg = firstMessageToMaster(messagesSentToMasterFrom(actorClient));
            assertThat(lfMsg.getPhase(), is(ActorLifecycleMessage.Phase.FAILED));
        }
    }

    @Test
    public void close_actor_and_closing_client_throws() throws Exception {
        clientFactory = new DummyClientFactory(null, actorClient);
        factory = new Actors(DUMMY_AGENT, clientFactory);

        doThrow(new RuntimeException()).when(actorClient).close();

        BaseActor actor = defaultTestActor();

        actor.close();
        verify(actorClient).close();

        // Check good flow of messages
        assertThat(actorClient.getSentMessages().size(), is(1));
        assertThat(messagesSentToMasterFrom(actorClient).size(), is(1));

        // Check closed message sent
        ActorLifecycleMessage lfMsg = firstMessageToMaster(messagesSentToMasterFrom(actorClient));
        assertThat(lfMsg.getPhase(), is(ActorLifecycleMessage.Phase.CLOSED));
    }

    @Test
    public void close_actor_calls_after() throws Exception {
        BaseActor actor = defaultTestActor();
        actor.close();

        // Assert after method was called
        assertTrue(((TestActor) actor.getInstance()).isAfterCalled());

        // Check good flow of messages
        assertThat(actorClient.getSentMessages().size(), is(1));
        assertThat(messagesSentToMasterFrom(actorClient).size(), is(1));

        // Check closed message sent
        ActorLifecycleMessage lfMsg = firstMessageToMaster(messagesSentToMasterFrom(actorClient));
        assertThat(lfMsg.getPhase(), is(ActorLifecycleMessage.Phase.CLOSED));
    }

    @Test
    public void close_actor_twice_closes_once() throws Exception {
        BaseActor actor = defaultTestActor();
        actor.close();

        verify(actorClient).close();

        // Assert after method was called
        assertTrue(((TestActor) actor.getInstance()).isAfterCalled());

        actor.close();

        verify(actorClient).close();

        // Check good flow of messages
        assertThat(actorClient.getSentMessages().size(), is(1));
        assertThat(messagesSentToMasterFrom(actorClient).size(), is(1));
    }

    @Test
    public void close_actor_and_after_method_throws() throws Exception {
        BaseActor actor = createActor(TestActorAfterThrows.class);
        actor.close();

        verify(actorClient).close();

        // Check good flow of messages
        assertThat(actorClient.getSentMessages().size(), is(1));
        assertThat(messagesSentToMasterFrom(actorClient).size(), is(1));

        // Check failed message sent
        ActorLifecycleMessage lfMsg = firstMessageToMaster(messagesSentToMasterFrom(actorClient));
        assertThat(lfMsg.getPhase(), is(ActorLifecycleMessage.Phase.FAILED));
    }

    @Test
    public void close_actor_and_sending_lifecycle_msg_throws() throws Exception {
        BaseActor actor = defaultTestActor();

        doThrow(new IllegalArgumentException()).when(actorClient).sendToActor(anyString(), any(Message.class));

        actor.close();

        verify(actorClient).close();

        // Check no messages sent
        assertTrue(actorClient.getSentMessages().isEmpty());
    }

    private void verifyIrrecoverableExceptions(String messageToActor) throws ValidationException {
        try (BaseActor actor = defaultTestActor()) {

            actor.onMessage(DUMMY_ACTOR, messageToActor);

            // Assert after method was called
            assertTrue(((TestActor) actor.getInstance()).isAfterCalled());

            // Check good flow of messages
            assertThat(actorClient.getSentMessages().size(), is(1));
            assertThat(messagesSentToMasterFrom(actorClient).size(), is(1));

            // Check failed message sent
            ActorLifecycleMessage lfMsg = firstMessageToMaster(messagesSentToMasterFrom(actorClient));
            assertThat(lfMsg.getPhase(), is(ActorLifecycleMessage.Phase.FAILED));
        }
    }

    private BaseActor createActor(final Class<?> actorClass) throws ValidationException {
        return (BaseActor) factory.create(DUMMY_ACTOR, actorClass.getName(), DUMMY_JSON_CONFIG);
    }

    private BaseActor defaultTestActor() throws ValidationException {
        return createActor(TestActor.class);
    }

    private List<Message<? extends Serializable>> messagesSentToMasterFrom(final RecorderOrchestratorActor actorClient) {
        return actorClient.getSentMessages().get(Constants.MASTER_ACTOR_NAME);
    }

    private <T extends Serializable> T firstMessageToMaster(final List<Message<? extends Serializable>> msgsToMaster) {
        return (T) ((AgentOutputMessage) msgsToMaster.get(0).data()).getData();
    }
}