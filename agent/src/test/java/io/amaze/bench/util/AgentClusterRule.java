package io.amaze.bench.util;

import io.amaze.bench.runtime.agent.AgentConfig;
import io.amaze.bench.runtime.cluster.jms.JMSAgentRegistryClusterClient;
import io.amaze.bench.runtime.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.test.JMSServerRule;
import org.junit.rules.ExternalResource;

/**
 * Created on 10/13/16.
 */
public final class AgentClusterRule extends ExternalResource {

    private final JMSServerRule jmsServerRule = new JMSServerRule();
    private JMSClient client;

    public AgentConfig agentConfig() {
        return ClusterConfigs.agentConfig(jmsServerRule);
    }

    public AgentRegistryClusterClient agentRegistryClusterClient() {
        return new JMSAgentRegistryClusterClient(client);
    }

    @Override
    protected void before() throws Throwable {
        jmsServerRule.init();
        client = jmsServerRule.createClient();
    }

    @Override
    protected void after() {
        client.close();
        jmsServerRule.close();
    }
}
