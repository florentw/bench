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
package io.amaze.bench.cluster.jms;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.cluster.RegistriesClusterClient;
import io.amaze.bench.cluster.registry.ActorRegistryListener;
import io.amaze.bench.cluster.registry.AgentRegistryListener;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.client.runtime.agent.Constants.REGISTRIES_TOPIC;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 9/24/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSRegistriesClusterClientTest {
    @Mock
    private JMSClient jmsClient;
    @Mock
    private JMSServer jmsServer;
    private RegistriesClusterClient registriesClient;

    @Before
    public void before() {
        registriesClient = new JMSRegistriesClusterClient(jmsServer, jmsClient);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(JMSRegistriesClusterClient.class);
        tester.testAllPublicInstanceMethods(registriesClient);
    }

    @Test
    public void creates_topic_when_initialized() throws JMSException {
        verify(jmsServer).createTopic(REGISTRIES_TOPIC);
        verifyNoMoreInteractions(jmsServer);
        verifyZeroInteractions(jmsClient);
    }

    @Test
    public void start_registry_listeners() throws JMSException {
        AgentRegistryListener agentRegistryListener = mock(AgentRegistryListener.class);
        ActorRegistryListener actorRegistryListener = mock(ActorRegistryListener.class);

        registriesClient.startRegistryListeners(agentRegistryListener, actorRegistryListener);

        verify(jmsClient).addTopicListener(eq(REGISTRIES_TOPIC), isA(JMSRegistriesTopicListener.class));
        verify(jmsClient).startListening();
        verifyNoMoreInteractions(jmsClient);
        verifyZeroInteractions(agentRegistryListener, actorRegistryListener);
    }

}