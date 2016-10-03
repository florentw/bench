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
package io.amaze.bench.runtime.cluster.jms;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.runtime.cluster.ActorCreationRequest;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.runtime.agent.AgentClientListener;
import io.amaze.bench.runtime.agent.AgentInputMessage;
import io.amaze.bench.shared.jms.JMSHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;

import static io.amaze.bench.runtime.agent.AgentTest.DUMMY_AGENT;
import static io.amaze.bench.shared.jms.JMSHelperTest.createTestBytesMessage;
import static org.mockito.Mockito.*;

/**
 * Created on 3/19/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSAgentMessageListenerTest {

    @Mock
    private AgentClientListener agentListener;
    private JMSAgentMessageListener listener;

    @Before
    public void before() {
        listener = new JMSAgentMessageListener(DUMMY_AGENT, agentListener);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(JMSAgentMessageListener.class);
        tester.testAllPublicInstanceMethods(listener);
    }

    @Test
    public void create_actor_calls_listener() throws IOException, ClassNotFoundException, JMSException {
        ActorCreationRequest creationRequest = new ActorCreationRequest(TestActor.DUMMY_CONFIG);

        AgentInputMessage toAgent = AgentInputMessage.createActor(DUMMY_AGENT, creationRequest);
        BytesMessage testBytesMessage = toBytesMessage(toAgent);

        listener.onMessage(testBytesMessage);

        verify(agentListener).onActorCreationRequest(eq(TestActor.DUMMY_CONFIG));
        verifyNoMoreInteractions(agentListener);
    }

    @Test
    public void create_actor_for_wrong_agent_does_nothing() throws IOException, ClassNotFoundException, JMSException {
        ActorCreationRequest creationRequest = new ActorCreationRequest(TestActor.DUMMY_CONFIG);
        AgentInputMessage toAgent = AgentInputMessage.createActor(DUMMY_AGENT + "2", creationRequest);
        BytesMessage testBytesMessage = toBytesMessage(toAgent);

        listener.onMessage(testBytesMessage);

        verifyNoMoreInteractions(agentListener);
    }

    @Test
    public void close_actor_calls_listener() throws IOException, ClassNotFoundException, JMSException {
        AgentInputMessage toAgent = AgentInputMessage.closeActor(DUMMY_AGENT, TestActor.DUMMY_ACTOR);
        BytesMessage testBytesMessage = toBytesMessage(toAgent);

        listener.onMessage(testBytesMessage);

        verify(agentListener).onActorCloseRequest(TestActor.DUMMY_ACTOR);
        verifyNoMoreInteractions(agentListener);
    }

    @Test
    public void error_on_message_does_not_throw() throws IOException, ClassNotFoundException, JMSException {
        listener.onMessage(mock(Message.class));

        verifyNoMoreInteractions(agentListener);
    }

    private BytesMessage toBytesMessage(final AgentInputMessage toAgent)
            throws IOException, ClassNotFoundException, JMSException {
        byte[] bytes = JMSHelper.convertToBytes(toAgent);
        return createTestBytesMessage(bytes);
    }
}