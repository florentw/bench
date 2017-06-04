/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
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

import io.amaze.bench.cluster.ClusterConfigFactory;
import io.amaze.bench.cluster.agent.AgentConfig;
import io.amaze.bench.cluster.jms.JMSAgentCluster;
import io.amaze.bench.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.shared.jgroups.JgroupsAgentCluster;
import org.junit.rules.ExternalResource;

import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Created on 10/13/16.
 */
public final class AgentClusterRule extends ExternalResource implements AgentCluster {

    private final AgentCluster delegateCluster;

    private AgentClusterRule(@NotNull final AgentCluster delegateCluster) {
        this.delegateCluster = requireNonNull(delegateCluster);
    }

    public static AgentClusterRule newJmsAgentCluster() {
        return new AgentClusterRule(new JMSAgentCluster());
    }

    public static AgentClusterRule newJgroupsAgentCluster() {
        return new AgentClusterRule(new JgroupsAgentCluster());
    }

    @Override
    public ClusterConfigFactory clusterConfigFactory() {
        return delegateCluster.clusterConfigFactory();
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
