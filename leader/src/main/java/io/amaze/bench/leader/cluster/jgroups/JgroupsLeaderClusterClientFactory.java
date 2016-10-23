package io.amaze.bench.leader.cluster.jgroups;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;
import io.amaze.bench.leader.cluster.LeaderClusterClientFactory;
import io.amaze.bench.leader.cluster.ResourceManagerClusterClient;
import io.amaze.bench.leader.cluster.registry.MetricsRepository;
import io.amaze.bench.leader.cluster.registry.MetricsRepositoryClusterClient;
import io.amaze.bench.runtime.cluster.ActorSender;
import io.amaze.bench.runtime.cluster.jgroups.JgroupsAbstractClusterClientFactory;
import io.amaze.bench.runtime.cluster.jgroups.JgroupsActorSender;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import org.jgroups.JChannel;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/23/16.
 */
public final class JgroupsLeaderClusterClientFactory extends JgroupsAbstractClusterClientFactory implements LeaderClusterClientFactory {

    public JgroupsLeaderClusterClientFactory(@NotNull final Config factoryConfig,
                                             @NotNull final ActorRegistry actorRegistry) {
        this(createJChannel(checkNotNull(factoryConfig)), checkNotNull(actorRegistry));
    }

    @VisibleForTesting
    JgroupsLeaderClusterClientFactory(@NotNull final JChannel jChannel, @NotNull final ActorRegistry actorRegistry) {
        super(jChannel, actorRegistry);
    }

    @Override
    public ActorSender actorSender() {
        return new JgroupsActorSender(jgroupsSender, actorRegistry);
    }

    @Override
    public ResourceManagerClusterClient createForResourceManager() {
        return new JgroupsResourceManagerClusterClient(jgroupsSender);
    }

    @Override
    public MetricsRepositoryClusterClient createForMetricsRepository(@NotNull final MetricsRepository metricsRepository) {
        checkNotNull(metricsRepository);
        JgroupsMetricsRepositoryClusterClient client = //
                new JgroupsMetricsRepositoryClusterClient(jgroupsClusterMember.listenerMultiplexer());
        client.startMetricsListener(metricsRepository.createClusterListener());
        return client;
    }
}
