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
package io.amaze.bench.runtime.actor;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.cluster.ClusterClientFactory;
import io.amaze.bench.cluster.ClusterConfigFactory;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.registry.ActorRegistryClusterClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.runtime.agent.AgentTest.DUMMY_AGENT;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

/**
 * Created on 4/9/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ActorManagersTest {

    private ActorManagers actorManagers;

    @Mock
    private ClusterClientFactory clientFactory;
    @Mock
    private ClusterConfigFactory configFactory;

    @Mock
    private ActorRegistryClusterClient actorRegistryClient;

    @Before
    public void before() {
        doReturn(actorRegistryClient).when(clientFactory).createForActorRegistry();
        actorManagers = new ActorManagers();
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(AgentKey.class, DUMMY_AGENT);

        tester.testAllPublicConstructors(ActorManagers.class);
        tester.testAllPublicInstanceMethods(actorManagers);
    }

    @Test
    public void can_supply_embedded_manager() {
        try (ActorManager embedded = actorManagers.createEmbedded(DUMMY_AGENT, clientFactory)) {
            assertNotNull(embedded);
            assertTrue(embedded instanceof EmbeddedActorManager);
        }
    }

    @Test
    public void can_supply_forked_manager() {
        try (ActorManager forked = actorManagers.createForked(DUMMY_AGENT, configFactory)) {
            assertNotNull(forked);
            assertTrue(forked instanceof ForkedActorManager);
        }
    }


}