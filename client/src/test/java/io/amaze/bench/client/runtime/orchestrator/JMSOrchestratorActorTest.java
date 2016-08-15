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
package io.amaze.bench.client.runtime.orchestrator;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.client.runtime.actor.Actor;
import io.amaze.bench.client.runtime.actor.TestActor;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.MessageListener;
import java.io.Serializable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 3/27/16.
 */
public final class JMSOrchestratorActorTest {

    private JMSClient jmsClient;
    private JMSOrchestratorActor client;

    @Before
    public void before() {
        jmsClient = mock(JMSClient.class);
        client = new JMSOrchestratorActor(jmsClient);
    }

    @After
    public void after() {
        client.close();
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(Message.class, new Message<>("", ""));
        tester.testAllPublicConstructors(JMSOrchestratorActor.class);
        tester.testAllPublicInstanceMethods(client);
    }

    @Test
    public void start_actor_listener() throws JMSException {
        Actor actor = mock(Actor.class);
        when(actor.name()).thenReturn(TestActor.DUMMY_ACTOR);

        client.startActorListener(actor);

        verify(jmsClient).addQueueListener(eq(TestActor.DUMMY_ACTOR), any(MessageListener.class));
        verify(jmsClient).startListening();
        verifyNoMoreInteractions(jmsClient);
    }

    @Test(expected = RuntimeException.class)
    public void start_actor_listener_and_listening_throws() throws JMSException {
        Actor actor = mock(Actor.class);
        when(actor.name()).thenReturn(TestActor.DUMMY_ACTOR);
        doThrow(new JMSException(new IllegalArgumentException())).when(jmsClient).startListening();

        client.startActorListener(actor);
    }

    @Test
    public void send_to_actors_sends_to_jms_queue() throws JMSException {
        Message<String> testMsg = getTestMsg();
        client.sendToActor(TestActor.DUMMY_ACTOR, testMsg);

        verify(jmsClient).sendToQueue(TestActor.DUMMY_ACTOR, testMsg);
        verifyNoMoreInteractions(jmsClient);
    }

    @Test(expected = RuntimeException.class)
    public void send_to_actors_sends_to_jms_queue_throws() throws JMSException {
        Message<String> testMsg = getTestMsg();
        doThrow(new JMSException(new IllegalArgumentException())).when(jmsClient).sendToQueue(anyString(),
                                                                                              any(Serializable.class));

        client.sendToActor(TestActor.DUMMY_ACTOR, testMsg);

        verify(jmsClient).sendToQueue(TestActor.DUMMY_ACTOR, testMsg);
        verifyNoMoreInteractions(jmsClient);
    }

    @Test
    public void close_closes_jms_client() throws JMSException {
        client.close();

        verify(jmsClient).close();
        verifyNoMoreInteractions(jmsClient);
    }

    private Message<String> getTestMsg() {
        return new Message<>(TestActor.DUMMY_ACTOR, "data");
    }
}