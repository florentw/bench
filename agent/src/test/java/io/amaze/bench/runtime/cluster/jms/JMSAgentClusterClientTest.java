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
import io.amaze.bench.runtime.actor.ActorInputMessage;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.agent.AgentClientListener;
import io.amaze.bench.runtime.agent.Constants;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.MessageListener;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 4/24/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSAgentClusterClientTest {

    private static final String TEST_AGENT = "agent1";

    @Mock
    private JMSClient jmsClient;
    @Mock
    private AgentClientListener agentClientListener;
    private JMSAgentClusterClient client;

    @Before
    public void before() {
        client = new JMSAgentClusterClient(jmsClient, TEST_AGENT);
    }

    @After
    public void after() {
        client.close();
    }

    @Test(expected = RuntimeException.class)
    public void invalid_hostname_throws() {
        new JMSAgentClusterClient(new JMSEndpoint("dummyhost", 1), TEST_AGENT);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorInputMessage.class, ActorInputMessage.init());
        tester.setDefault(ActorKey.class, DUMMY_ACTOR);

        tester.testAllPublicConstructors(JMSAgentClusterClient.class);
        tester.testAllPublicInstanceMethods(client);
    }

    @Test
    public void start_agent_listener() throws JMSException {
        client.startAgentListener(TEST_AGENT, agentClientListener);

        verify(jmsClient).addTopicListener(eq(Constants.AGENTS_TOPIC), any(MessageListener.class));
        verify(jmsClient).startListening();
        verifyNoMoreInteractions(jmsClient);
    }

    @Test(expected = RuntimeException.class)
    public void start_agent_listener_and_listening_throws() throws JMSException {

        doThrow(new JMSException(new IllegalArgumentException())).when(jmsClient).startListening();

        client.startAgentListener(TEST_AGENT, agentClientListener);
    }

    @Test
    public void close_closes_jms_client() throws JMSException {
        client.close();

        verify(jmsClient).close();
        verifyNoMoreInteractions(jmsClient);
    }
}
