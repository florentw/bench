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

import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.cluster.ActorClusterClient;
import io.amaze.bench.runtime.cluster.AgentClusterClient;
import io.amaze.bench.runtime.cluster.ClusterClientFactory;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.shared.jgroups.JgroupsClusterMember;
import org.jgroups.JChannel;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/2/16.
 */
public final class JgroupsClusterClientFactory implements ClusterClientFactory {

    private final JgroupsClusterMember jgroupsClusterMember;
    private final JgroupsSender jgroupsSender;
    private final ActorRegistry actorRegistry;

    public JgroupsClusterClientFactory(@NotNull JChannel jChannel, @NotNull final ActorRegistry actorRegistry) {
        this.actorRegistry = checkNotNull(actorRegistry);
        jgroupsSender = new JgroupsSender(jChannel, actorRegistry);
        jgroupsClusterMember = new JgroupsClusterMember(jChannel);
    }

    @Override
    public AgentClusterClient createForAgent(@NotNull final String agent) {
        checkNotNull(agent);
        return new JgroupsAgentClusterClient(jgroupsClusterMember.listenerMultiplexer(), jgroupsSender);
    }

    @Override
    public ActorClusterClient createForActor(@NotNull final ActorKey actor) {
        checkNotNull(actor);
        return new JgroupsActorClusterClient(jgroupsClusterMember.listenerMultiplexer(), jgroupsSender);
    }

    @Override
    public ActorRegistryClusterClient createForActorRegistry() {
        return new JgroupsActorRegistryClusterClient(jgroupsClusterMember.listenerMultiplexer(),
                                                     jgroupsClusterMember.stateMultiplexer(),
                                                     actorRegistry);
    }
}
