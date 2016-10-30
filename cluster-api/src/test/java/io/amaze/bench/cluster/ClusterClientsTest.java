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
package io.amaze.bench.cluster;

import com.google.common.testing.NullPointerTester;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.amaze.bench.Endpoint;
import io.amaze.bench.cluster.actor.ActorClusterClient;
import io.amaze.bench.cluster.actor.ActorKey;
import io.amaze.bench.cluster.agent.AgentClusterClient;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.cluster.registry.AgentRegistry;
import io.amaze.bench.cluster.registry.AgentRegistryClusterClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.constraints.NotNull;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Created on 10/14/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ClusterClientsTest {

    @Mock
    private ActorRegistry actorRegistry;

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(ClusterClients.class);
        tester.testAllPublicStaticMethods(ClusterClients.class);
    }

    @Test
    public void factory_is_created_from_cluster_config() {
        Config clusterConfig = ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":\"" + DummyClusterClientFactory.class.getName() + "\"," + //
                                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":{}}");

        AgentClusterClientFactory factory = ClusterClients.newFactory(AgentClusterClientFactory.class,
                                                                      clusterConfig,
                                                                      actorRegistry);

        assertNotNull(factory);
        assertThat(factory, instanceOf(DummyClusterClientFactory.class));
    }

    @Test(expected = RuntimeException.class)
    public void factory_propagates_class_not_found() {
        Config clusterConfig = ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":\"" + String.class.getName() + "\"," + //
                                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":{}}");
        ClusterClients.newFactory(AgentClusterClientFactory.class, clusterConfig, actorRegistry);
    }

    public static class DummyClusterClientFactory implements AgentClusterClientFactory {
        @Override
        public Endpoint localEndpoint() {
            return null;
        }

        @Override
        public AgentClusterClient createForAgent(@NotNull final AgentKey agent) {
            return null;
        }

        @Override
        public ActorClusterClient createForActor(@NotNull final ActorKey actor) {
            return null;
        }

        @Override
        public ActorRegistryClusterClient createForActorRegistry() {
            return null;
        }

        @Override
        public AgentRegistryClusterClient createForAgentRegistry(@NotNull final AgentRegistry agentRegistry) {
            return null;
        }

        @Override
        public ClusterConfigFactory clusterConfigFactory() {
            return null;
        }

        @Override
        public void close() {

        }
    }

}