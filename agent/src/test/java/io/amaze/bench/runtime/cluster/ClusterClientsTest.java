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
package io.amaze.bench.runtime.cluster;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.util.ClusterConfigs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.util.ClusterConfigs.JMS_AGENT_FACTORY_CLASS;
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
        ClusterClientFactory factory = ClusterClients.newFactory(ClusterClientFactory.class,
                                                                 ClusterConfigs.defaultConfig(),
                                                                 actorRegistry);

        assertNotNull(factory);
        assertThat(factory, instanceOf(JMS_AGENT_FACTORY_CLASS));
    }

    @Test(expected = RuntimeException.class)
    public void factory_propagates_class_not_found() {
        ClusterClients.newFactory(ClusterClientFactory.class,
                                  ClusterConfigs.invalidClassClusterConfig(),
                                  actorRegistry);
    }

}