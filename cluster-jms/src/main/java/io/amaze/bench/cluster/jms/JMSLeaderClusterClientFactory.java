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
import com.google.common.base.Throwables;
import com.typesafe.config.Config;
import io.amaze.bench.cluster.actor.ActorSender;
import io.amaze.bench.cluster.leader.LeaderClusterClientFactory;
import io.amaze.bench.cluster.leader.ResourceManagerClusterClient;
import io.amaze.bench.cluster.metric.MetricsRepository;
import io.amaze.bench.cluster.metric.MetricsRepositoryClusterClient;
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.cluster.registry.AgentRegistry;
import io.amaze.bench.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.shared.jms.*;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Throwables.propagate;
import static java.util.Objects.requireNonNull;

/**
 * Created on 10/23/16.
 */
public final class JMSLeaderClusterClientFactory implements LeaderClusterClientFactory {

    private final JMSServer server;
    private final JMSEndpoint serverEndpoint;
    private final ActorRegistry actorRegistry;
    private volatile JMSClient senderJmsClient;

    public JMSLeaderClusterClientFactory(@NotNull final Config factoryConfig,
                                         @NotNull final ActorRegistry actorRegistry) {
        this.actorRegistry = requireNonNull(actorRegistry);
        try {
            this.serverEndpoint = new JMSEndpoint(factoryConfig);
            this.server = new FFMQServer(serverEndpoint);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @VisibleForTesting
    public JMSLeaderClusterClientFactory(@NotNull final JMSServer server,
                                         @NotNull final JMSEndpoint serverEndpoint,
                                         @NotNull final ActorRegistry actorRegistry) {

        this.actorRegistry = requireNonNull(actorRegistry);
        this.serverEndpoint = requireNonNull(serverEndpoint);
        this.server = requireNonNull(server);
    }

    @Override
    public ActorSender actorSender() {
        senderJmsClient = createJmsClient();
        return new JMSActorSender(senderJmsClient);
    }

    @Override
    public ResourceManagerClusterClient createForResourceManager() {
        return new JMSResourceManagerClusterClient(server, serverEndpoint);
    }

    @Override
    public MetricsRepositoryClusterClient createForMetricsRepository(@NotNull final MetricsRepository metricsRepository) {
        requireNonNull(metricsRepository);
        MetricsRepositoryClusterClient clusterClient = new JMSMetricsRepositoryClusterClient(serverEndpoint);
        clusterClient.startMetricsListener(metricsRepository.createClusterListener());
        return clusterClient;
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
    public void close() {
        if (senderJmsClient != null) {
            senderJmsClient.close();
        }
        server.close();
    }

    private JMSClient createJmsClient() {
        JMSClient client;
        try {
            client = new FFMQClient(serverEndpoint);
        } catch (JMSException e) {
            throw Throwables.propagate(e);
        }
        return client;
    }
}
