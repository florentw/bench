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
package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;
import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.cluster.ActorClusterClient;
import io.amaze.bench.runtime.cluster.AgentClusterClient;
import io.amaze.bench.runtime.cluster.ClusterClientFactory;
import io.amaze.bench.runtime.cluster.ClusterConfigFactory;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.shared.jgroups.JgroupsEndpoint;
import org.jgroups.JChannel;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/2/16.
 */
public final class JgroupsClusterClientFactory extends JgroupsAbstractClusterClientFactory implements ClusterClientFactory {

    private JgroupsClusterConfigFactory jgroupsClusterConfigFactory;

    public JgroupsClusterClientFactory(@NotNull final Config factoryConfig,
                                       @NotNull final ActorRegistry actorRegistry) {
        this(createJChannel(factoryConfig), checkNotNull(actorRegistry), factoryConfig);
    }

    @VisibleForTesting
    JgroupsClusterClientFactory(@NotNull final JChannel jChannel,
                                @NotNull final ActorRegistry actorRegistry,
                                @NotNull final Config factoryConfig) {
        super(jChannel, actorRegistry);
        checkNotNull(factoryConfig);

        jgroupsClusterConfigFactory = new JgroupsClusterConfigFactory(factoryConfig);
    }

    @Override
    public Endpoint localEndpoint() {
        return new JgroupsEndpoint(jChannel.getAddress());
    }

    @Override
    public AgentClusterClient createForAgent(@NotNull final String agent) {
        checkNotNull(agent);
        return new JgroupsAgentClusterClient(jgroupsClusterMember.listenerMultiplexer(), jgroupsSender, actorRegistry);
    }

    @Override
    public ActorClusterClient createForActor(@NotNull final ActorKey actor) {
        checkNotNull(actor);
        return new JgroupsActorClusterClient(localEndpoint(),
                                             jgroupsClusterMember.listenerMultiplexer(),
                                             jgroupsSender,
                                             actorRegistry);
    }

    @Override
    public ClusterConfigFactory clusterConfigFactory() {
        return jgroupsClusterConfigFactory;
    }

}
