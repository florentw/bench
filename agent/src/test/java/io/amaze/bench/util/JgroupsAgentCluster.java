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

import com.google.common.base.Throwables;
import io.amaze.bench.cluster.ClusterConfigFactory;
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.cluster.registry.AgentRegistry;
import io.amaze.bench.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.runtime.agent.AgentConfig;
import io.amaze.bench.runtime.cluster.jgroups.JgroupsAgentRegistryClusterClient;
import io.amaze.bench.runtime.cluster.jgroups.JgroupsClusterClientFactory;
import io.amaze.bench.runtime.cluster.jgroups.JgroupsClusterConfigFactory;
import io.amaze.bench.shared.jgroups.JgroupsClusterMember;
import org.jgroups.JChannel;

/**
 * Created on 10/28/16.
 */
final class JgroupsAgentCluster implements AgentCluster {

    private JChannel jChannel;
    private JgroupsClusterClientFactory clusterClientFactory;
    private JgroupsAgentRegistryClusterClient jgroupsClusterClient;

    @Override
    public ClusterConfigFactory clusterConfigFactory() {
        return new JgroupsClusterConfigFactory(ClusterConfigs.jgroupsFactoryConfig());
    }

    @Override
    public AgentConfig agentConfig() {
        return ClusterConfigs.jgroupsAgentConfig();
    }

    @Override
    public AgentRegistryClusterClient agentRegistryClusterClient() {
        return jgroupsClusterClient;
    }

    @Override
    public void before() {
        initJgroups();
        clusterClientFactory = new JgroupsClusterClientFactory(ClusterConfigs.jgroupsFactoryConfig(),
                                                               new ActorRegistry());
    }

    @Override
    public void after() {
        clusterClientFactory.close();
        jChannel.close();
    }

    private void initJgroups() {
        try {
            jChannel = new JChannel("fast.xml");
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
