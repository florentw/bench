package io.amaze.bench.runtime.agent;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/13/16.
 */
public final class AgentConfig {

    public static final String AGENT_CLUSTER_TAG = "AgentCluster";

    private final Config clusterConfig;

    public AgentConfig(final File configFile) {
        checkNotNull(configFile);

        Config agentConfig = ConfigFactory.parseFile(configFile);
        clusterConfig = agentConfig.getConfig(AGENT_CLUSTER_TAG);
    }

    public AgentConfig(final Config clusterConfig) {
        this.clusterConfig = checkNotNull(clusterConfig);
    }

    public Config clusterConfig() {
        return clusterConfig;
    }

    public String clusterConfigJson() {
        return clusterConfig.root().render(ConfigRenderOptions.concise());
    }

    public Config toConfig() {
        return ConfigFactory.parseString("{\"" + AgentConfig.AGENT_CLUSTER_TAG + "\":" + clusterConfigJson() + "}");
    }

    public String toJson() {
        return toConfig().root().render(ConfigRenderOptions.concise());
    }
}
