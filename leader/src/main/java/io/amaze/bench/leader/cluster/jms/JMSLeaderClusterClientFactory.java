package io.amaze.bench.leader.cluster.jms;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;
import io.amaze.bench.leader.cluster.LeaderClusterClientFactory;
import io.amaze.bench.leader.cluster.ResourceManagerClusterClient;
import io.amaze.bench.leader.cluster.registry.MetricsRepository;
import io.amaze.bench.leader.cluster.registry.MetricsRepositoryClusterClient;
import io.amaze.bench.runtime.cluster.jms.JMSActorRegistryClusterClient;
import io.amaze.bench.runtime.cluster.jms.JMSAgentRegistryClusterClient;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.runtime.cluster.registry.AgentRegistry;
import io.amaze.bench.runtime.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.shared.jms.FFMQServer;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSServer;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

/**
 * Created on 10/23/16.
 */
public final class JMSLeaderClusterClientFactory implements LeaderClusterClientFactory {

    private final JMSServer server;
    private final JMSEndpoint serverEndpoint;
    private final ActorRegistry actorRegistry;

    public JMSLeaderClusterClientFactory(@NotNull final Config factoryConfig,
                                         @NotNull final ActorRegistry actorRegistry) {
        this.actorRegistry = checkNotNull(actorRegistry);
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

        this.actorRegistry = checkNotNull(actorRegistry);
        this.serverEndpoint = checkNotNull(serverEndpoint);
        this.server = checkNotNull(server);
    }

    @Override
    public ResourceManagerClusterClient createForResourceManager() {
        return new JMSResourceManagerClusterClient(server, serverEndpoint);
    }

    @Override
    public MetricsRepositoryClusterClient createForMetricsRepository(@NotNull final MetricsRepository metricsRepository) {
        checkNotNull(metricsRepository);
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
        checkNotNull(agentRegistry);
        AgentRegistryClusterClient agentRegistryClient = new JMSAgentRegistryClusterClient(serverEndpoint);
        agentRegistryClient.startRegistryListener(agentRegistry.createClusterListener());
        return agentRegistryClient;
    }

    @Override
    public void close() {
        server.close();
    }
}
