package io.amaze.bench.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.amaze.bench.runtime.agent.AgentConfig;
import io.amaze.bench.runtime.cluster.ClusterClients;
import io.amaze.bench.runtime.cluster.jms.JMSClusterClientFactory;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.test.JMSServerRule;

/**
 * Created on 10/13/16.
 */
public final class ClusterConfigs {

    private static final String DUMMY_HOST = "dummy";
    private static final int DUMMY_PORT = 1337;

    public static Config dummyClusterConfig() {
        return ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":\"" + JMSClusterClientFactory.class.getName() + "\"," + //
                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":" + new JMSEndpoint(
                DUMMY_HOST,
                DUMMY_PORT).toConfig().root().render(ConfigRenderOptions.concise()) + "}");
    }

    public static Config clusterConfig(final JMSServerRule server) {
        return agentConfig(server).clusterConfig();
    }

    public static AgentConfig agentConfig(final JMSServerRule server) {
        Config clusterConfig = ConfigFactory.parseString("{" + //
                                                                 "\"" + ClusterClients.FACTORY_CLASS + "\":\"" + JMSClusterClientFactory.class.getName() + "\"," + //
                                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":" + server.getEndpoint().toConfig().root().render(
                ConfigRenderOptions.concise()) + "}");
        return new AgentConfig(clusterConfig);
    }

}
