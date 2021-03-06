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
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.cluster.registry.AgentRegistry;
import io.amaze.bench.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.shared.jgroups.JgroupsClusterMember;
import org.jgroups.JChannel;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Throwables.propagate;
import static java.util.Objects.requireNonNull;

/**
 * Created on 10/23/16.
 */
public abstract class JgroupsAbstractClusterClientFactory {

    public static final String XML_CONFIG = "xmlConfig";

    protected final JgroupsSender jgroupsSender;
    protected final JgroupsClusterMember jgroupsClusterMember;
    protected final ActorRegistry actorRegistry;
    final JChannel jChannel;
    private final JgroupsActorRegistryClusterClient registryClusterClient;

    protected JgroupsAbstractClusterClientFactory(@NotNull final JChannel jChannel,
                                                  @NotNull final ActorRegistry actorRegistry) {
        this.jChannel = requireNonNull(jChannel);
        this.actorRegistry = requireNonNull(actorRegistry);

        jgroupsClusterMember = new JgroupsClusterMember(jChannel);
        registryClusterClient = new JgroupsActorRegistryClusterClient(jgroupsClusterMember.listenerMultiplexer(),
                                                                      jgroupsClusterMember.stateMultiplexer(),
                                                                      jgroupsClusterMember.viewMultiplexer(),
                                                                      actorRegistry);
        jgroupsSender = new JgroupsSender(jChannel);
        jgroupsClusterMember.join();
        registryClusterClient.startRegistryListener(actorRegistry.createClusterListener());
    }

    public ActorRegistryClusterClient createForActorRegistry() {
        return registryClusterClient;
    }

    public AgentRegistryClusterClient createForAgentRegistry(@NotNull final AgentRegistry agentRegistry) {
        requireNonNull(agentRegistry);
        AgentRegistryClusterClient clusterClient = new JgroupsAgentRegistryClusterClient(jgroupsClusterMember.listenerMultiplexer(),
                                                                                         jgroupsClusterMember.stateMultiplexer(),
                                                                                         jgroupsClusterMember.viewMultiplexer(),
                                                                                         agentRegistry);
        clusterClient.startRegistryListener(agentRegistry.createClusterListener());
        return clusterClient;
    }

    public void close() {
        registryClusterClient.close();
        jChannel.close();
    }

    @VisibleForTesting
    public JChannel getJChannel() {
        return jChannel;
    }

    protected static JChannel createJChannel(final Config clusterConfig) {
        requireNonNull(clusterConfig);
        try {
            return new JChannel(clusterConfig.getString(XML_CONFIG));
        } catch (Exception e) { // NOSONAR - No choice here
            throw propagate(e);
        }
    }
}
