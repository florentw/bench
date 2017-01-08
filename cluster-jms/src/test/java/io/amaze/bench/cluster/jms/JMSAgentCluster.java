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

import com.google.common.base.Throwables;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.amaze.bench.cluster.ClusterClients;
import io.amaze.bench.cluster.ClusterConfigFactory;
import io.amaze.bench.cluster.agent.AgentConfig;
import io.amaze.bench.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSServerRule;
import io.amaze.bench.util.AgentCluster;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;

/**
 * Created on 10/28/16.
 */
public final class JMSAgentCluster implements AgentCluster {

    private final JMSServerRule jmsServerRule = new JMSServerRule();
    private ClusterConfigFactory clusterConfigFactory;

    @Override
    public ClusterConfigFactory clusterConfigFactory() {
        return clusterConfigFactory;
    }

    @Override
    public AgentConfig agentConfig() {
        return new AgentConfig(jmsClusterConfig(jmsServerRule.getEndpoint()));
    }

    @Override
    public AgentRegistryClusterClient agentRegistryClusterClient() {
        return new JMSAgentRegistryClusterClient(jmsServerRule.getEndpoint());
    }

    @Override
    public void before() {
        jmsServerRule.init();
        clusterConfigFactory = new JMSClusterConfigFactory(jmsServerRule.getEndpoint());
        try {
            jmsServerRule.getServer().createQueue(DUMMY_ACTOR.getName());
        } catch (JMSException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void after() {
        jmsServerRule.close();
    }

    private static Config jmsClusterConfig(final JMSEndpoint endpoint) {
        return ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":\"" + JMSClusterClientFactory.class.getName() + "\"," + //
                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":" + jmsFactoryConfigJson(
                endpoint) + "}");
    }

    private static String jmsFactoryConfigJson(final JMSEndpoint endpoint) {
        return endpoint.toConfig().root().render(ConfigRenderOptions.concise());
    }
}
