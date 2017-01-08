/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
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
import io.amaze.bench.cluster.AgentClusterClientFactory;
import io.amaze.bench.cluster.actor.ActorClusterClient;
import io.amaze.bench.cluster.agent.AgentClusterClient;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.registry.*;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSServerRule;
import io.amaze.bench.shared.test.IntegrationTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created on 8/16/16.
 */
@RunWith(MockitoJUnitRunner.class)
@Category(IntegrationTest.class)
public final class JMSClusterClientFactoryTest {

    @Rule
    public final JMSServerRule jmsServerRule = new JMSServerRule();
    @Mock
    private ActorRegistry actorRegistry;
    @Mock
    private AgentRegistry agentRegistry;

    private AgentClusterClientFactory clientFactory;

    @Before
    public void init() {
        clientFactory = new JMSClusterClientFactory(jmsServerRule.getEndpoint(), actorRegistry);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(JMSClusterClientFactory.class);
        tester.testAllPublicInstanceMethods(clientFactory);
    }

    @Test
    public void constructor_with_config_parameter_does_not_throw() {
        new JMSClusterClientFactory(jmsServerRule.getEndpoint().toConfig(), actorRegistry);
    }

    @Test
    public void createForAgent_returns_instance() {
        AgentClusterClient client = clientFactory.createForAgent(new AgentKey("agent"));

        assertNotNull(client);
    }

    @Test
    public void createForActor_returns_instance() {
        ActorClusterClient client = clientFactory.createForActor(TestActor.DUMMY_ACTOR);

        assertNotNull(client);
    }

    @Test
    public void createForActorRegistry_adds_listener_and_returns_instance() {
        when(actorRegistry.createClusterListener()).thenReturn(mock(ActorRegistryListener.class));

        ActorRegistryClusterClient client = clientFactory.createForActorRegistry();

        assertNotNull(client);
        verify(actorRegistry).createClusterListener();
        verifyNoMoreInteractions(actorRegistry);
    }

    @Test
    public void createForAgentRegistry_adds_listener_and_returns_instance() {
        when(agentRegistry.createClusterListener()).thenReturn(mock(AgentRegistryListener.class));

        AgentRegistryClusterClient client = clientFactory.createForAgentRegistry(agentRegistry);

        assertNotNull(client);
        verify(agentRegistry).createClusterListener();
        verifyNoMoreInteractions(agentRegistry);
    }

    @Test
    public void clusterConfigFactory_returns_instance() {
        assertNotNull(clientFactory.clusterConfigFactory());
    }

    @Test
    public void close_does_nothing() {
        clientFactory.close();

        verifyZeroInteractions(actorRegistry);
    }

    @Test
    public void localEndpoint_returns_JMSEndpoint() {
        assertNotNull(clientFactory.localEndpoint());
        assertThat(clientFactory.localEndpoint(), instanceOf(JMSEndpoint.class));
    }

}