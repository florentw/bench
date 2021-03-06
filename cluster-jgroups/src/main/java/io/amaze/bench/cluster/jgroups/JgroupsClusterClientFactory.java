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
package io.amaze.bench.cluster.jgroups;

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
import io.amaze.bench.shared.jgroups.JgroupsEndpoint;
import org.jgroups.JChannel;

import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Created on 10/2/16.
 */
public final class JgroupsClusterClientFactory extends JgroupsAbstractClusterClientFactory implements AgentClusterClientFactory {

    private JgroupsClusterConfigFactory jgroupsClusterConfigFactory;

    public JgroupsClusterClientFactory(@NotNull final Config factoryConfig,
                                       @NotNull final ActorRegistry actorRegistry) {
        this(createJChannel(factoryConfig), requireNonNull(actorRegistry), factoryConfig);
    }

    @VisibleForTesting
    JgroupsClusterClientFactory(@NotNull final JChannel jChannel,
                                @NotNull final ActorRegistry actorRegistry,
                                @NotNull final Config factoryConfig) {
        super(jChannel, actorRegistry);
        requireNonNull(factoryConfig);

        jgroupsClusterConfigFactory = new JgroupsClusterConfigFactory(factoryConfig);
    }

    @Override
    public Endpoint localEndpoint() {
        return new JgroupsEndpoint(jChannel.getAddress());
    }

    @Override
    public AgentClusterClient createForAgent(@NotNull final AgentKey agent) {
        requireNonNull(agent);
        return new JgroupsAgentClusterClient(jgroupsClusterMember.listenerMultiplexer(), jgroupsSender, actorRegistry);
    }

    @Override
    public ActorClusterClient createForActor(@NotNull final ActorKey actor) {
        requireNonNull(actor);
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
