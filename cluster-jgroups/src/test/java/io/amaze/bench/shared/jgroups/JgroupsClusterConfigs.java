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
package io.amaze.bench.shared.jgroups;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.amaze.bench.cluster.ClusterClients;
import io.amaze.bench.cluster.jgroups.JgroupsClusterClientFactory;
import io.amaze.bench.cluster.jgroups.JgroupsLeaderClusterClientFactory;
import io.amaze.bench.util.TestClusterConfigs;

/**
 * Created on 10/31/16.
 */
public final class JgroupsClusterConfigs implements TestClusterConfigs {

    static final String JGROUPS_XML_PROTOCOLS = "fast.xml";

    private static final String JGROUPS_FACTORY_CLASS = JgroupsClusterClientFactory.class.getName();
    private static final String JGROUPS_LEADER_FACTORY_CLASS = JgroupsLeaderClusterClientFactory.class.getName();

    public static Config invalidClassClusterConfig() {
        return ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":\"dummy\"," + //
                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":{}}");
    }

    private static Config jgroupsFactoryConfig() {
        return ConfigFactory.parseString("{\"" + JgroupsClusterClientFactory.XML_CONFIG + "\":\"" + JGROUPS_XML_PROTOCOLS + "\"}");
    }

    private static String jgroupsFactoryConfigJson() {
        return jgroupsFactoryConfig().root().render(ConfigRenderOptions.concise());
    }

    @Override
    public Config leaderConfig() {
        return ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":\"" + JGROUPS_LEADER_FACTORY_CLASS + "\"," + //
                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":" + jgroupsFactoryConfigJson() + "}");
    }

    @Override
    public Config clusterConfig() {
        return ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":\"" + JGROUPS_FACTORY_CLASS + "\"," + //
                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":" + jgroupsFactoryConfigJson() + "}");
    }

    @Override
    public Config agentFactoryConfig() {
        return jgroupsFactoryConfig();
    }
}
