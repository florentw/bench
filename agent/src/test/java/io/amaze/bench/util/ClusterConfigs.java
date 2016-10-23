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

    public static AgentConfig jmsAgentConfig(final JMSServerRule server) {
        Config clusterConfig = jmsClusterConfig(server.getEndpoint());
        return new AgentConfig(clusterConfig);
    }

    public static Config jgroupsFactoryConfig() {
        return ConfigFactory.parseString("{\"" + JgroupsClusterClientFactory.XML_CONFIG + "\":\"fast.xml\"}");
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
