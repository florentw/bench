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
import io.amaze.bench.runtime.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.shared.jgroups.JgroupsClusterMember;
import io.amaze.bench.shared.jgroups.JgroupsEndpoint;
import org.jgroups.JChannel;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

/**
 * Created on 10/2/16.
 */
public final class JgroupsClusterClientFactory implements ClusterClientFactory {

    public static final String XML_CONFIG = "xmlConfig";

    private final JgroupsClusterMember jgroupsClusterMember;
    private final JgroupsSender jgroupsSender;
    private final JChannel jChannel;

    private JgroupsActorRegistryClusterClient registryClusterClient;
    private JgroupsClusterConfigFactory jgroupsClusterConfigFactory;

    public JgroupsClusterClientFactory(@NotNull final Config factoryConfig,
                                       @NotNull final ActorRegistry actorRegistry) {
        checkNotNull(factoryConfig);
        checkNotNull(actorRegistry);

        jChannel = createJChannel(factoryConfig);

        jgroupsSender = new JgroupsSender(jChannel, actorRegistry);
        jgroupsClusterMember = new JgroupsClusterMember(jChannel);
        registryClusterClient = new JgroupsActorRegistryClusterClient(jgroupsClusterMember.listenerMultiplexer(),
                                                                      jgroupsClusterMember.stateMultiplexer(),
                                                                      jgroupsClusterMember.viewMultiplexer(),
                                                                      actorRegistry);
        jgroupsClusterConfigFactory = new JgroupsClusterConfigFactory();

        jgroupsClusterMember.join();
        registryClusterClient.startRegistryListener(actorRegistry.createClusterListener());
    }

    @Override
    public Endpoint getLocalEndpoint() {
        return new JgroupsEndpoint(jChannel.getAddress());
    }

    @Override
    public AgentClusterClient createForAgent(@NotNull final String agent) {
        checkNotNull(agent);
        return new JgroupsAgentClusterClient(jgroupsClusterMember.listenerMultiplexer(), jgroupsSender);
    }

    @Override
    public ActorClusterClient createForActor(@NotNull final ActorKey actor) {
        checkNotNull(actor);
        return new JgroupsActorClusterClient(getLocalEndpoint(),
                                             jgroupsClusterMember.listenerMultiplexer(),
                                             jgroupsSender);
    }

    @Override
    public ActorRegistryClusterClient createForActorRegistry() {
        return registryClusterClient;
    }

    @Override
    public ClusterConfigFactory clusterConfigFactory() {
        return jgroupsClusterConfigFactory;
    }

    @Override
    public void close() {
        jChannel.close();
    }

    @VisibleForTesting
    public JChannel getJChannel() {
        return jChannel;
    }

    private JChannel createJChannel(final Config clusterConfig) {
        try {
            return new JChannel(clusterConfig.getString(XML_CONFIG));
        } catch (Exception e) {
            throw propagate(e);
        }
    }

}
