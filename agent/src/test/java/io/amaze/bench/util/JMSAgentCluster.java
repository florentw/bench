package io.amaze.bench.util;

import io.amaze.bench.runtime.agent.AgentConfig;
import io.amaze.bench.runtime.cluster.jms.JMSAgentRegistryClusterClient;
import io.amaze.bench.runtime.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.shared.jms.JMSServerRule;

/**
 * Created on 10/28/16.
 */
final class JMSAgentCluster implements AgentCluster {

    private final JMSServerRule jmsServerRule = new JMSServerRule();

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
    }

    @Override
    public void after() {
        jmsServerRule.close();
    }
}
