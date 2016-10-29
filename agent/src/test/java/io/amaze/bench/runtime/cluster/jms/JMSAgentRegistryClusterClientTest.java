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
import io.amaze.bench.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.cluster.registry.AgentRegistryListener;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.cluster.agent.Constants.AGENT_REGISTRY_TOPIC;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * Created on 9/25/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSAgentRegistryClusterClientTest {

    @Mock
    private JMSClient jmsClient;
    @Mock
    private JMSServer jmsServer;
    private AgentRegistryClusterClient agentRegistryClient;

    @Before
    public void before() {
        agentRegistryClient = new JMSAgentRegistryClusterClient(jmsClient);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(JMSAgentRegistryClusterClient.class);
        tester.testAllPublicInstanceMethods(agentRegistryClient);
    }

    @Test
    public void start_registry_listeners() throws JMSException {
        AgentRegistryListener agentRegistryListener = mock(AgentRegistryListener.class);

        this.agentRegistryClient.startRegistryListener(agentRegistryListener);

        verify(jmsClient).addTopicListener(eq(AGENT_REGISTRY_TOPIC), isA(JMSAgentRegistryTopicListener.class));
        verify(jmsClient).startListening();
        verifyNoMoreInteractions(jmsClient);
        verifyZeroInteractions(agentRegistryListener);
    }

}