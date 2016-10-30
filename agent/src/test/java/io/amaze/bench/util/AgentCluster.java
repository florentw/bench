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

import io.amaze.bench.cluster.ClusterConfigFactory;
import io.amaze.bench.cluster.leader.registry.AgentRegistryClusterClient;
import io.amaze.bench.runtime.agent.AgentConfig;

/**
 * Created on 10/28/16.
 */
public interface AgentCluster {

    ClusterConfigFactory clusterConfigFactory();

    AgentConfig agentConfig();

    AgentRegistryClusterClient agentRegistryClusterClient();

    void before();

    void after();

}
