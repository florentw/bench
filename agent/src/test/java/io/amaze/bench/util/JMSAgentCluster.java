package io.amaze.bench.util;

import com.google.common.base.Throwables;
import io.amaze.bench.runtime.agent.AgentConfig;
import io.amaze.bench.runtime.cluster.ClusterConfigFactory;
import io.amaze.bench.runtime.cluster.jms.JMSAgentRegistryClusterClient;
import io.amaze.bench.runtime.cluster.jms.JMSClusterConfigFactory;
import io.amaze.bench.runtime.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSServerRule;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;

/**
 * Created on 10/28/16.
 */
final class JMSAgentCluster implements AgentCluster {

    private final JMSServerRule jmsServerRule = new JMSServerRule();
    private ClusterConfigFactory clusterConfigFactory;

    @Override
    public ClusterConfigFactory clusterConfigFactory() {
        return clusterConfigFactory;
    }

    @Override
    public AgentConfig agentConfig() {
        return ClusterConfigs.jmsAgentConfig(jmsServerRule);
    }

    @Override
    public AgentRegistryClusterClient agentRegistryClusterClient() {
        return new JMSAgentRegistryClusterClient(jmsServerRule.getEndpoint());
    }

    @Override
    public void before() {
        jmsServerRule.init();
        clusterConfigFactory = new JMSClusterConfigFactory(jmsServerRule.getEndpoint());
        try {
            jmsServerRule.getServer().createQueue(DUMMY_ACTOR.getName());
        } catch (JMSException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void after() {
        jmsServerRule.close();
    }
}
