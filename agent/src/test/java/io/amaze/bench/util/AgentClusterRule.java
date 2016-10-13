package io.amaze.bench.util;

import com.typesafe.config.Config;
import io.amaze.bench.runtime.agent.AgentConfig;
import io.amaze.bench.runtime.cluster.ClusterClientFactory;
import io.amaze.bench.runtime.cluster.ClusterClients;
import io.amaze.bench.shared.test.JMSServerRule;
import org.junit.rules.ExternalResource;

/**
 * Created on 10/13/16.
 */
public final class AgentClusterRule extends ExternalResource {

    private final JMSServerRule jmsServerRule = new JMSServerRule();

    public JMSServerRule jmsServerRule() {
        return jmsServerRule;
    }

    public Config clusterConfig() {
        return ClusterConfigs.clusterConfig(jmsServerRule);
    }

    public AgentConfig agentConfig() {
        return ClusterConfigs.agentConfig(jmsServerRule);
    }

    public ClusterClientFactory clusterClientFactory() {
        return ClusterClients.newFactory(clusterConfig());
    }

    @Override
    protected void before() throws Throwable {
        jmsServerRule.init();
    }

    @Override
    protected void after() {
        jmsServerRule.close();
    }
}
