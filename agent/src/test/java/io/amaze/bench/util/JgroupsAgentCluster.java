package io.amaze.bench.util;

import com.google.common.base.Throwables;
import io.amaze.bench.runtime.agent.AgentConfig;
import io.amaze.bench.runtime.cluster.jgroups.JgroupsAgentRegistryClusterClient;
import io.amaze.bench.runtime.cluster.jgroups.JgroupsClusterClientFactory;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.AgentRegistry;
import io.amaze.bench.runtime.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.shared.jgroups.JgroupsClusterMember;
import org.jgroups.JChannel;

/**
 * Created on 10/28/16.
 */
final class JgroupsAgentCluster implements AgentCluster {

    private JgroupsClusterClientFactory clusterClientFactory;
    private JChannel jChannel;
    private JgroupsAgentRegistryClusterClient jgroupsClusterClient;

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
