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
package io.amaze.bench.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.amaze.bench.runtime.agent.AgentConfig;
import io.amaze.bench.runtime.cluster.ClusterClientFactory;
import io.amaze.bench.runtime.cluster.ClusterClients;
import io.amaze.bench.runtime.cluster.jgroups.JgroupsClusterClientFactory;
import io.amaze.bench.runtime.cluster.jms.JMSClusterClientFactory;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSServerRule;

/**
 * Created on 10/13/16.
 */
public final class ClusterConfigs {

    public static final Class<? extends ClusterClientFactory> JMS_AGENT_FACTORY_CLASS = JMSClusterClientFactory.class;
    public static final Class<? extends ClusterClientFactory> JGROUPS_FACTORY_CLASS = JgroupsClusterClientFactory.class;
    public static final Class<? extends ClusterClientFactory> DEFAULT_FACTORY_CLASS = JMS_AGENT_FACTORY_CLASS;
    public static final String JGROUPS_XML_PROTOCOLS = "fast.xml";
    private static final JMSEndpoint DUMMY_ENDPOINT = new JMSEndpoint("dummy", 1337);

    public static Config defaultConfig() {
        return dummyConfigFor(DEFAULT_FACTORY_CLASS);
    }

    public static Config dummyConfigFor(final Class<? extends ClusterClientFactory> factoryClass) {
        String factoryConfig;
        if (factoryClass.equals(JMS_AGENT_FACTORY_CLASS)) {
            factoryConfig = jmsFactoryConfigJson(DUMMY_ENDPOINT);
        } else if (factoryClass.equals(JGROUPS_FACTORY_CLASS)) {
            factoryConfig = jgroupsFactoryConfigJson();
        } else {
            throw new UnsupportedOperationException();
        }

        return ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":\"" + factoryClass.getName() + "\"," + //
                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":" + factoryConfig + "}");
    }

    public static Config invalidClassClusterConfig() {
        return ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":\"dummy\"," + //
                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":{}}");
    }

    public static AgentConfig jgroupsAgentConfig() {
        return new AgentConfig(jgroupsClusterConfig());
    }

    public static AgentConfig jmsAgentConfig(final JMSServerRule server) {
        return new AgentConfig(jmsClusterConfig(server.getEndpoint()));
    }

    public static Config jgroupsFactoryConfig() {
        return ConfigFactory.parseString("{\"" + JgroupsClusterClientFactory.XML_CONFIG + "\":\"" + JGROUPS_XML_PROTOCOLS + "\"}");
    }

    public static Config jgroupsClusterConfig() {
        return ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":\"" + JGROUPS_FACTORY_CLASS.getName() + "\"," + //
                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":" + jgroupsFactoryConfigJson() + "}");
    }

    public static Config leaderJgroupsClusterConfig(final Class<?> factoryClass) {
        return ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":\"" + factoryClass.getName() + "\"," + //
                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":" + jgroupsFactoryConfigJson() + "}");
    }

    public static Config jmsClusterConfig(final JMSEndpoint endpoint) {
        return ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":\"" + JMS_AGENT_FACTORY_CLASS.getName() + "\"," + //
                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":" + jmsFactoryConfigJson(
                endpoint) + "}");
    }

    public static Config leaderJmsClusterConfig(final JMSEndpoint endpoint, final Class<?> factoryClass) {
        return ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":\"" + factoryClass.getName() + "\"," + //
                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":" + jmsFactoryConfigJson(
                endpoint) + "}");
    }

    private static String jgroupsFactoryConfigJson() {
        return jgroupsFactoryConfig().root().render(ConfigRenderOptions.concise());
    }

    private static String jmsFactoryConfigJson(final JMSEndpoint endpoint) {
        return endpoint.toConfig().root().render(ConfigRenderOptions.concise());
    }
}
