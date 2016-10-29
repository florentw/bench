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
