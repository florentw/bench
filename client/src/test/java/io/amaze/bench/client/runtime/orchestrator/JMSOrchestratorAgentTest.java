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
import io.amaze.bench.client.runtime.agent.AgentClientListener;
import io.amaze.bench.client.runtime.agent.Constants;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.MessageListener;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 4/24/16.
 */
public final class JMSOrchestratorAgentTest {

    private static final String TEST_AGENT = "agent1";

    private JMSClient jmsClient;
    private JMSOrchestratorAgent client;

    @Before
    public void before() {
        jmsClient = mock(JMSClient.class);
        client = new JMSOrchestratorAgent(jmsClient);
    }

    @After
    public void after() {
        client.close();
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(Message.class, new Message<>("", ""));
        tester.testAllPublicConstructors(JMSOrchestratorAgent.class);
        tester.testAllPublicInstanceMethods(client);
    }

    @Test
    public void start_agent_listener() throws JMSException {
        client.startAgentListener(TEST_AGENT, mock(AgentClientListener.class));

        verify(jmsClient).addTopicListener(eq(Constants.AGENTS_ACTOR_NAME), any(MessageListener.class));
        verify(jmsClient).startListening();
        verifyNoMoreInteractions(jmsClient);
    }

    @Test(expected = RuntimeException.class)
    public void start_agent_listener_and_listening_throws() throws JMSException {

        doThrow(new JMSException(new IllegalArgumentException())).when(jmsClient).startListening();

        client.startAgentListener(TEST_AGENT, mock(AgentClientListener.class));
    }

    @Test
    public void close_closes_jms_client() throws JMSException {
        client.close();

        verify(jmsClient).close();
        verifyNoMoreInteractions(jmsClient);
    }
}
