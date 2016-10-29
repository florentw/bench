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
package io.amaze.bench.leader.cluster.jms;

import com.google.common.testing.NullPointerTester;
import com.typesafe.config.Config;
import io.amaze.bench.api.After;
import io.amaze.bench.leader.cluster.LeaderClusterClientFactory;
import io.amaze.bench.leader.cluster.ResourceManagerClusterClient;
import io.amaze.bench.leader.cluster.registry.MetricsRepository;
import io.amaze.bench.leader.cluster.registry.MetricsRepositoryClusterClient;
import io.amaze.bench.leader.cluster.registry.MetricsRepositoryListener;
import io.amaze.bench.runtime.cluster.registry.*;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSServer;
import io.amaze.bench.shared.jms.JMSServerRule;
import io.amaze.bench.shared.test.IntegrationTest;
import io.amaze.bench.shared.util.Network;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created on 10/23/16.
 */
@RunWith(MockitoJUnitRunner.class)
@Category(IntegrationTest.class)
public final class JMSLeaderClusterClientFactoryTest {

    @Rule
    public final JMSServerRule jmsServerRule = new JMSServerRule();
    @Mock
    private ActorRegistry actorRegistry;

    private LeaderClusterClientFactory clientFactory;
    @Mock
    private MetricsRepository metricsRepository;
    @Mock
    private AgentRegistry agentRegistry;

    @Before
    public void init() {
        clientFactory = new JMSLeaderClusterClientFactory(jmsServerRule.getServer(),
                                                          jmsServerRule.getEndpoint(),
                                                          actorRegistry);
    }

    @After
    public void close() {
        clientFactory.close();
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(JMSEndpoint.class, jmsServerRule.getEndpoint());

        tester.testAllPublicConstructors(JMSLeaderClusterClientFactory.class);
        tester.testAllPublicInstanceMethods(clientFactory);
    }

    @Test
    public void constructor_with_config_parameter_creates_server() {
        Config config = new JMSEndpoint(Network.LOCALHOST, Network.findFreePort()).toConfig();

        LeaderClusterClientFactory clientFactory = new JMSLeaderClusterClientFactory(config, actorRegistry);
        clientFactory.close();
    }

    @Test(expected = RuntimeException.class)
    public void constructor_with_config_parameter_invalid_config_throws() {
        Config config = new JMSEndpoint("dummy", 1337).toConfig();

        new JMSLeaderClusterClientFactory(config, actorRegistry);
    }

    @Test
    public void createForResourceManager_returns_instance() {
        ResourceManagerClusterClient client = clientFactory.createForResourceManager();

        assertNotNull(client);
    }

    @Test
    public void createForMetricsRepository_starts_listener_and_returns_instance() {
        when(metricsRepository.createClusterListener()).thenReturn(mock(MetricsRepositoryListener.class));

        MetricsRepositoryClusterClient client = clientFactory.createForMetricsRepository(metricsRepository);

        assertNotNull(client);
        verify(metricsRepository).createClusterListener();
        verifyNoMoreInteractions(metricsRepository);
    }

    @Test
    public void createForActorRegistry_starts_listener_and_returns_instance() {
        when(actorRegistry.createClusterListener()).thenReturn(mock(ActorRegistryListener.class));

        ActorRegistryClusterClient client = clientFactory.createForActorRegistry();

        assertNotNull(client);
        verify(actorRegistry).createClusterListener();
        verifyNoMoreInteractions(actorRegistry);
    }

    @Test
    public void createForAgentRegistry_starts_listener_and_returns_instance() {
        when(agentRegistry.createClusterListener()).thenReturn(mock(AgentRegistryListener.class));

        AgentRegistryClusterClient client = clientFactory.createForAgentRegistry(agentRegistry);

        assertNotNull(client);
        verify(agentRegistry).createClusterListener();
        verifyNoMoreInteractions(agentRegistry);
    }

    @Test
    public void close_closes_jms_server() {
        JMSServer jmsServer = mock(JMSServer.class);
        LeaderClusterClientFactory clientFactory2 = new JMSLeaderClusterClientFactory(jmsServer,
                                                                                      jmsServerRule.getEndpoint(),
                                                                                      actorRegistry);

        clientFactory2.close();

        verify(jmsServer).close();
        verifyNoMoreInteractions(jmsServer);
    }
}