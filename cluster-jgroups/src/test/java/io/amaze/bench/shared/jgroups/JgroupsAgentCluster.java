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

import com.google.common.base.Throwables;
import io.amaze.bench.cluster.ClusterConfigFactory;
import io.amaze.bench.cluster.agent.AgentConfig;
import io.amaze.bench.cluster.jgroups.JgroupsAgentRegistryClusterClient;
import io.amaze.bench.cluster.jgroups.JgroupsClusterClientFactory;
import io.amaze.bench.cluster.jgroups.JgroupsClusterConfigFactory;
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.cluster.registry.AgentRegistry;
import io.amaze.bench.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.util.AgentCluster;
import org.jgroups.JChannel;

/**
 * Created on 10/28/16.
 */
public final class JgroupsAgentCluster implements AgentCluster {

    private final JgroupsClusterConfigs configs;
    private JChannel jChannel;
    private JgroupsClusterClientFactory clusterClientFactory;
    private JgroupsAgentRegistryClusterClient jgroupsClusterClient;

    public JgroupsAgentCluster() {
        configs = new JgroupsClusterConfigs();
    }

    @Override
    public ClusterConfigFactory clusterConfigFactory() {
        return new JgroupsClusterConfigFactory(configs.agentFactoryConfig());
    }

    @Override
    public AgentConfig agentConfig() {
        return new AgentConfig(configs.clusterConfig());
    }

    @Override
    public AgentRegistryClusterClient agentRegistryClusterClient() {
        return jgroupsClusterClient;
    }

    @Override
    public void before() {
        initJgroups();
        clusterClientFactory = new JgroupsClusterClientFactory(configs.agentFactoryConfig(), new ActorRegistry());
    }

    @Override
    public void after() {
        clusterClientFactory.close();
        jChannel.close();
    }

    private void initJgroups() {
        try {
            jChannel = new JChannel(JgroupsClusterConfigs.JGROUPS_XML_PROTOCOLS);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        JgroupsClusterMember clusterMember = new JgroupsClusterMember(jChannel);
        jgroupsClusterClient = new JgroupsAgentRegistryClusterClient(clusterMember.listenerMultiplexer(),
                                                                     clusterMember.stateMultiplexer(),
                                                                     clusterMember.viewMultiplexer(),
                                                                     new AgentRegistry());
        clusterMember.join();
    }

}
