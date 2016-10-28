package io.amaze.bench.util;

import io.amaze.bench.runtime.agent.AgentConfig;
import io.amaze.bench.runtime.cluster.registry.AgentRegistryClusterClient;

/**
 * Created on 10/28/16.
 */
public interface AgentCluster {

    AgentConfig agentConfig();

    AgentRegistryClusterClient agentRegistryClusterClient();

    void before();

    void after();

}
