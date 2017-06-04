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
package io.amaze.bench.cluster.jms;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;
import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.AgentClusterClientFactory;
import io.amaze.bench.cluster.ClusterConfigFactory;
import io.amaze.bench.cluster.Endpoint;
import io.amaze.bench.cluster.actor.ActorClusterClient;
import io.amaze.bench.cluster.agent.AgentClusterClient;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.cluster.registry.AgentRegistry;
import io.amaze.bench.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.shared.jms.JMSEndpoint;

import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Created on 3/3/16.
 */
public final class JMSClusterClientFactory implements AgentClusterClientFactory {

    private static final JMSEndpoint DUMMY_JMS_ENDPOINT = new JMSEndpoint("dummy", 1337);

    private final JMSEndpoint serverEndpoint;
    private final ActorRegistry actorRegistry;

    public JMSClusterClientFactory(@NotNull final Config factoryConfig, @NotNull final ActorRegistry actorRegistry) {
        requireNonNull(factoryConfig);
        this.actorRegistry = requireNonNull(actorRegistry);
        this.serverEndpoint = new JMSEndpoint(factoryConfig);
    }

    @VisibleForTesting
    JMSClusterClientFactory(@NotNull final JMSEndpoint serverEndpoint, @NotNull final ActorRegistry actorRegistry) {
        this.serverEndpoint = requireNonNull(serverEndpoint);
        this.actorRegistry = requireNonNull(actorRegistry);
    }

    @Override
    public Endpoint localEndpoint() {
        return DUMMY_JMS_ENDPOINT;
    }

    @Override
    public AgentClusterClient createForAgent(@NotNull AgentKey agent) {
        return new JMSAgentClusterClient(serverEndpoint, requireNonNull(agent));
    }

    @Override
    public ActorClusterClient createForActor(@NotNull ActorKey actor) {
        return new JMSActorClusterClient(serverEndpoint, requireNonNull(actor));
    }

    @Override
    public ActorRegistryClusterClient createForActorRegistry() {
        ActorRegistryClusterClient registryClusterClient = new JMSActorRegistryClusterClient(serverEndpoint);
        registryClusterClient.startRegistryListener(actorRegistry.createClusterListener());
        return registryClusterClient;
    }

    @Override
    public AgentRegistryClusterClient createForAgentRegistry(@NotNull final AgentRegistry agentRegistry) {
        requireNonNull(agentRegistry);
        AgentRegistryClusterClient agentRegistryClient = new JMSAgentRegistryClusterClient(serverEndpoint);
        agentRegistryClient.startRegistryListener(agentRegistry.createClusterListener());
        return agentRegistryClient;
    }

    @Override
    public ClusterConfigFactory clusterConfigFactory() {
        return new JMSClusterConfigFactory(serverEndpoint);
    }

    @Override
    public void close() {
        // Nothing to close
    }
}

