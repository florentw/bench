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
package io.amaze.bench.cluster.leader.jgroups;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;
import io.amaze.bench.cluster.actor.ActorSender;
import io.amaze.bench.cluster.leader.LeaderClusterClientFactory;
import io.amaze.bench.cluster.leader.ResourceManagerClusterClient;
import io.amaze.bench.cluster.metric.MetricsRepository;
import io.amaze.bench.cluster.metric.MetricsRepositoryClusterClient;
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.jgroups.JgroupsAbstractClusterClientFactory;
import io.amaze.bench.runtime.cluster.jgroups.JgroupsActorSender;
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
