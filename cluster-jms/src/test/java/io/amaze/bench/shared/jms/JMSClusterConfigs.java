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
package io.amaze.bench.shared.jms;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.amaze.bench.cluster.ClusterClients;
import io.amaze.bench.cluster.jms.JMSClusterClientFactory;
import io.amaze.bench.cluster.jms.JMSLeaderClusterClientFactory;
import io.amaze.bench.shared.util.Network;
import io.amaze.bench.util.TestClusterConfigs;

/**
 * Do not remove, is referenced directly in configuration.
 */
public final class JMSClusterConfigs implements TestClusterConfigs {

    public static final String JMS_AGENT_FACTORY_CLASS = JMSClusterClientFactory.class.getName();

    private final JMSEndpoint endpoint;

    public JMSClusterConfigs() {
        endpoint = new JMSEndpoint(Network.LOCALHOST, Network.findFreePort());
    }

    @Override
    public Config leaderConfig() {
        return leaderJmsClusterConfig(JMSLeaderClusterClientFactory.class.getName());
    }

    @Override
    public Config clusterConfig() {
        return jmsClusterConfig();
    }

    @Override
    public Config agentFactoryConfig() {
        return endpoint.toConfig();
    }

    private static String jmsFactoryConfigJson(final JMSEndpoint endpoint) {
        return endpoint.toConfig().root().render(ConfigRenderOptions.concise());
    }

    private Config jmsClusterConfig() {
        return ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":\"" + JMS_AGENT_FACTORY_CLASS + "\"," + //
                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":" + jmsFactoryConfigJson(
                endpoint) + "}");
    }

    private Config leaderJmsClusterConfig(final String factoryClass) {
        return ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":\"" + factoryClass + "\"," + //
                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":" + jmsFactoryConfigJson(
                endpoint) + "}");
    }
}
