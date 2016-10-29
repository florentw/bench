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
package io.amaze.bench.leader.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.api.After;
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.cluster.registry.ActorRegistryListener;
import io.amaze.bench.leader.cluster.ResourceManagerClusterClient;
import io.amaze.bench.leader.cluster.registry.MetricsRepository;
import io.amaze.bench.leader.cluster.registry.MetricsRepositoryClusterClient;
import io.amaze.bench.leader.cluster.registry.MetricsRepositoryListener;
import io.amaze.bench.shared.test.IntegrationTest;
import io.amaze.bench.shared.util.Network;
import io.amaze.bench.util.ClusterConfigs;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.ViewId;
import org.jgroups.stack.IpAddress;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.UnknownHostException;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created on 10/23/16.
 */
@RunWith(MockitoJUnitRunner.class)
@Category(IntegrationTest.class)
public final class JgroupsLeaderClusterClientFactoryTest {

    @Mock
    private JChannel jChannel;
    @Mock
    private ActorRegistry actorRegistry;
    @Mock
    private Address address;

    private JgroupsLeaderClusterClientFactory clientFactory;
    @Mock
    private MetricsRepository metricsRepository;

    @Before
    public void init() throws UnknownHostException {
        View initialView = new View(new ViewId(), new Address[]{new IpAddress(Network.LOCALHOST, 1337)});
        when(jChannel.view()).thenReturn(initialView);
        when(jChannel.getAddress()).thenReturn(address);
        when(actorRegistry.createClusterListener()).thenReturn(mock(ActorRegistryListener.class));

        clientFactory = new JgroupsLeaderClusterClientFactory(jChannel, actorRegistry);
    }

    @After
    public void close() {
        clientFactory.close();
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicInstanceMethods(clientFactory);
    }

    @Test
    public void constructor_with_config_parameter_creates_jChannel() {
        JgroupsLeaderClusterClientFactory clientFactory2 = new JgroupsLeaderClusterClientFactory(ClusterConfigs.jgroupsFactoryConfig(),
                                                                                                 actorRegistry);

        assertNotNull(clientFactory2.getJChannel());

        clientFactory2.close();
    }

    @Test
    public void createForResourceManager_returns_instance() {
        ResourceManagerClusterClient client = clientFactory.createForResourceManager();

        assertNotNull(client);
    }

    @Test
    public void createForMetricsRepository_adds_listener_and_returns_instance() {
        when(metricsRepository.createClusterListener()).thenReturn(mock(MetricsRepositoryListener.class));

        MetricsRepositoryClusterClient client = clientFactory.createForMetricsRepository(metricsRepository);

        assertNotNull(client);
        verify(metricsRepository).createClusterListener();
        verifyNoMoreInteractions(metricsRepository);
    }

}