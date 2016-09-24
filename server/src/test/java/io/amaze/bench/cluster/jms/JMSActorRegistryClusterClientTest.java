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
import io.amaze.bench.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.cluster.registry.ActorRegistryListener;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.client.runtime.agent.Constants.ACTOR_REGISTRY_TOPIC;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * Created on 9/25/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSActorRegistryClusterClientTest {

    @Mock
    private JMSClient jmsClient;
    @Mock
    private JMSServer jmsServer;
    private ActorRegistryClusterClient actorRegistryClient;

    @Before
    public void before() {
        actorRegistryClient = new JMSActorRegistryClusterClient(jmsServer, jmsClient);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(JMSActorRegistryClusterClient.class);
        tester.testAllPublicInstanceMethods(actorRegistryClient);
    }

    @Test
    public void creates_topic_when_initialized() throws JMSException {
        verify(jmsServer).createTopic(ACTOR_REGISTRY_TOPIC);
        verifyNoMoreInteractions(jmsServer);
        verifyZeroInteractions(jmsClient);
    }

    @Test
    public void start_registry_listeners() throws JMSException {
        ActorRegistryListener actorRegistryListener = mock(ActorRegistryListener.class);

        actorRegistryClient.startRegistryListener(actorRegistryListener);

        verify(jmsClient).addTopicListener(eq(ACTOR_REGISTRY_TOPIC), isA(JMSActorRegistryTopicListener.class));
        verify(jmsClient).startListening();
        verifyNoMoreInteractions(jmsClient);
        verifyZeroInteractions(actorRegistryListener);
    }

}