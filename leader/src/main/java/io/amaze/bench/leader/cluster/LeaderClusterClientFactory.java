package io.amaze.bench.leader.cluster;

import io.amaze.bench.leader.cluster.registry.MetricsRepository;
import io.amaze.bench.leader.cluster.registry.MetricsRepositoryClusterClient;
import io.amaze.bench.runtime.cluster.ActorSender;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.runtime.cluster.registry.AgentRegistry;
import io.amaze.bench.runtime.cluster.registry.AgentRegistryClusterClient;

import javax.validation.constraints.NotNull;
import java.io.Closeable;

/**
 * Created on 10/23/16.
 */
public interface LeaderClusterClientFactory extends Closeable {

    @NotNull
    ActorSender actorSender();

    @NotNull
    ResourceManagerClusterClient createForResourceManager();

    @NotNull
    MetricsRepositoryClusterClient createForMetricsRepository(@NotNull MetricsRepository metricsRepository);

    @NotNull
    ActorRegistryClusterClient createForActorRegistry();

    @NotNull
    AgentRegistryClusterClient createForAgentRegistry(@NotNull AgentRegistry agentRegistry);

    @Override
    void close();
}
