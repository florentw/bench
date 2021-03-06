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
package io.amaze.bench.cluster;

import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.actor.ActorClusterClient;
import io.amaze.bench.cluster.agent.AgentClusterClient;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.cluster.registry.AgentRegistry;
import io.amaze.bench.cluster.registry.AgentRegistryClusterClient;

import javax.validation.constraints.NotNull;
import java.io.Closeable;

/**
 * Factory to create clients on the agent side to communicate with the cluster.
 * <ul>
 * <li>Provides {@link AgentClusterClient} instances for the agent
 * to listen to incoming messages.</li>
 * <li>Provides {@link ActorClusterClient} instances for actors to listen to incoming messages
 * and be able to send back.</li>
 * <li>Provides {@link ActorRegistryClusterClient} instances for the actor registry to interact with the cluster.</li>
 * </ul>
 *
 * @see AgentClusterClient
 * @see ActorClusterClient
 * @see ActorRegistryClusterClient
 * @see ClusterConfigFactory
 */
public interface AgentClusterClientFactory extends Closeable {

    /**
     * Can be used by some implementations to expose their local endpoint for point to point communication.
     *
     * @return A cluster endpoint that can be used to talk to this client.
     */
    @NotNull
    Endpoint localEndpoint();

    /**
     * Creates a cluster client instance for Agents to connect to the cluster.
     *
     * @param agent The agent to create an {@link AgentClusterClient} instance for.
     * @return A non null instance of {@link AgentClusterClient}
     */
    @NotNull
    AgentClusterClient createForAgent(@NotNull AgentKey agent);

    /**
     * Creates a cluster client instance for Actors to connect to the cluster.
     *
     * @param actor The actor to create an {@link ActorClusterClient} instance for.
     * @return A non null instance of {@link ActorClusterClient}
     */
    @NotNull
    ActorClusterClient createForActor(@NotNull ActorKey actor);

    @NotNull
    ActorRegistryClusterClient createForActorRegistry();

    @NotNull
    AgentRegistryClusterClient createForAgentRegistry(@NotNull AgentRegistry agentRegistry);

    @NotNull
    ClusterConfigFactory clusterConfigFactory();

    @Override
    void close();

}
