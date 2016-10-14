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
import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.LifecycleMessage;
import io.amaze.bench.runtime.agent.AgentLifecycleMessage;
import io.amaze.bench.runtime.agent.AgentRegistrationMessage;
import io.amaze.bench.runtime.cluster.registry.AgentRegistryListener;
import io.amaze.bench.runtime.message.Message;
import io.amaze.bench.shared.jms.JMSHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import java.io.IOException;

import static io.amaze.bench.runtime.agent.AgentTest.DUMMY_AGENT;
import static io.amaze.bench.shared.jms.JMSHelperTest.createTestBytesMessage;
import static org.mockito.Mockito.*;

/**
 * Created on 3/29/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSAgentRegistryTopicListenerTest {

    @Mock
    private AgentRegistryListener agentRegistryListener;
    @Mock
    private Endpoint endpoint;

    private JMSAgentRegistryTopicListener messageListener;

    @Before
    public void before() {
        messageListener = new JMSAgentRegistryTopicListener(agentRegistryListener);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(JMSAgentRegistryTopicListener.class);
        tester.testAllPublicInstanceMethods(messageListener);
    }

    @Test
    public void corrupted_message_does_nothing() throws IOException, ClassNotFoundException, JMSException {
        messageListener.onMessage(mock(javax.jms.Message.class));

        verifyNoMoreInteractions(agentRegistryListener);
    }

    @Test
    public void message_of_invalid_type_does_nothing() throws IOException, JMSException {
        byte[] data = JMSHelper.convertToBytes(new Message<>("", "Hello"));
        BytesMessage msg = createTestBytesMessage(data);

        messageListener.onMessage(msg);

        verifyNoMoreInteractions(agentRegistryListener);
    }

    @Test
    public void agent_registered() throws IOException, JMSException {
        AgentRegistrationMessage regMsg = AgentRegistrationMessage.create("dummy-agent", endpoint);
        BytesMessage msg = toBytesMessage(AgentLifecycleMessage.created(regMsg));

        messageListener.onMessage(msg);

        verify(agentRegistryListener).onAgentRegistration(regMsg);
        verifyNoMoreInteractions(agentRegistryListener);
    }

    @Test
    public void agent_sign_off() throws IOException, JMSException {
        BytesMessage msg = toBytesMessage(AgentLifecycleMessage.closed(DUMMY_AGENT));

        messageListener.onMessage(msg);

        verify(agentRegistryListener).onAgentSignOff(DUMMY_AGENT);
        verifyNoMoreInteractions(agentRegistryListener);
    }

    @Test
    public void agent_fails() throws IOException, JMSException {
        Throwable throwable = new IllegalArgumentException();
        BytesMessage msg = toBytesMessage(AgentLifecycleMessage.failed(DUMMY_AGENT, throwable));

        messageListener.onMessage(msg);

        verify(agentRegistryListener).onAgentFailed(eq(DUMMY_AGENT), any(IllegalArgumentException.class));
        verifyNoMoreInteractions(agentRegistryListener);
    }

    private BytesMessage toBytesMessage(final LifecycleMessage lifecycleMessage) throws IOException, JMSException {
        Message<LifecycleMessage> message = new Message<>("", lifecycleMessage);
        final byte[] data = JMSHelper.convertToBytes(message);
        return createTestBytesMessage(data);
    }
}