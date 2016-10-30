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
package io.amaze.bench.cluster.leader;

import io.amaze.bench.cluster.actor.ActorSender;
import io.amaze.bench.cluster.metric.MetricsRepository;
import io.amaze.bench.cluster.metric.MetricsRepositoryClusterClient;
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.cluster.registry.AgentRegistry;
import io.amaze.bench.cluster.registry.AgentRegistryClusterClient;

import javax.validation.constraints.NotNull;
import java.io.Closeable;

/**
 * Created on 10/23/16.
 */
public interface LeaderClusterClientFactory extends Closeable {

    /**
     * Used by the leader to send messages to actors in the cluster.
     *
     * @return A non-null instance of {@link ActorSender}
     */
    @NotNull
    ActorSender actorSender();

    /**
     * Creates a client for the ResourceManager to communicate with the cluster.
     *
     * @return A non-null instance of {@link ResourceManagerClusterClient}
     */
    @NotNull
    ResourceManagerClusterClient createForResourceManager();

    /**
     * Creates a client for the MetricsRepository to communicate with the cluster.
     *
     * @param metricsRepository Repository that will be notified by cluster messages.
     * @return A non-null instance of {@link MetricsRepositoryClusterClient}
     */
    @NotNull
    MetricsRepositoryClusterClient createForMetricsRepository(@NotNull MetricsRepository metricsRepository);

    /**
     * Creates a client for the {@link ActorRegistry} to communicate with the cluster.
     *
     * @return A non-null instance of {@link ActorRegistryClusterClient}
     */
    @NotNull
    ActorRegistryClusterClient createForActorRegistry();

    /**
     * Creates a client for the {@link AgentRegistry} to communicate with the cluster.
     *
     * @return A non-null instance of {@link AgentRegistryClusterClient}
     */
    @NotNull
    AgentRegistryClusterClient createForAgentRegistry(@NotNull AgentRegistry agentRegistry);

    @Override
    void close();
}
