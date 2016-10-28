package io.amaze.bench.util;

import io.amaze.bench.runtime.agent.AgentConfig;
import io.amaze.bench.runtime.cluster.registry.AgentRegistryClusterClient;
import org.junit.rules.ExternalResource;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/13/16.
 */
public final class AgentClusterRule extends ExternalResource implements AgentCluster {

    private final AgentCluster delegateCluster;

    private AgentClusterRule(@NotNull final AgentCluster delegateCluster) {
        this.delegateCluster = checkNotNull(delegateCluster);
    }

    public static AgentClusterRule newJgroupsAgentCluster() {
        return new AgentClusterRule(new JMSAgentCluster());
    }

    public static AgentClusterRule newJmsAgentCluster() {
        return new AgentClusterRule(new JgroupsAgentCluster());
    }

    @Override
    public AgentConfig agentConfig() {
        return delegateCluster.agentConfig();
    }

    @Override
    public AgentRegistryClusterClient agentRegistryClusterClient() {
        return delegateCluster.agentRegistryClusterClient();
    }

    @Override
    public void before() {
        delegateCluster.before();
    }

    @Override
    public void after() {
        delegateCluster.after();
    }
}
