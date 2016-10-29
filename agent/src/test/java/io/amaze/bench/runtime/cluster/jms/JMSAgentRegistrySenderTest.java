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
import io.amaze.bench.runtime.agent.AgentLifecycleMessage;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.runtime.agent.Constants.AGENT_REGISTRY_TOPIC;
import static io.amaze.bench.util.Matchers.isAgentLifecycle;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created on 10/19/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSAgentRegistrySenderTest {

    private static final String TEST_AGENT = "agent";

    @Mock
    private JMSClient client;

    private JMSAgentRegistrySender sender;

    @Before
    public void init() {
        sender = new JMSAgentRegistrySender(client, TEST_AGENT);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(JMSAgentRegistrySender.class);
        tester.testAllPublicInstanceMethods(sender);
    }

    @Test
    public void send_to_agent_registry_sends_to_topic() throws JMSException {
        AgentLifecycleMessage agentLifecycleMessage = AgentLifecycleMessage.closed(TEST_AGENT);

        sender.send(agentLifecycleMessage);

        verify(client).sendToTopic(eq(AGENT_REGISTRY_TOPIC),
                                   argThat(isAgentLifecycle(TEST_AGENT, agentLifecycleMessage)));
        verifyNoMoreInteractions(client);
    }
}