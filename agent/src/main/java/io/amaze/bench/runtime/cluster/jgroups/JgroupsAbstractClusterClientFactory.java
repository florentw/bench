package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.runtime.cluster.registry.AgentRegistry;
import io.amaze.bench.runtime.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.shared.jgroups.JgroupsClusterMember;
import org.jgroups.JChannel;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

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
        this.jChannel = checkNotNull(jChannel);
        this.actorRegistry = checkNotNull(actorRegistry);

        jgroupsClusterMember = new JgroupsClusterMember(jChannel);
        registryClusterClient = new JgroupsActorRegistryClusterClient(jgroupsClusterMember.listenerMultiplexer(),
                                                                      jgroupsClusterMember.stateMultiplexer(),
                                                                      jgroupsClusterMember.viewMultiplexer(),
                                                                      actorRegistry);
        jgroupsSender = new JgroupsSender(jChannel);
        jgroupsClusterMember.join();
        registryClusterClient.startRegistryListener(actorRegistry.createClusterListener());
    }

    protected static JChannel createJChannel(final Config clusterConfig) {
        try {
            return new JChannel(clusterConfig.getString(XML_CONFIG));
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    public ActorRegistryClusterClient createForActorRegistry() {
        return registryClusterClient;
    }

    public AgentRegistryClusterClient createForAgentRegistry(@NotNull final AgentRegistry agentRegistry) {
        checkNotNull(agentRegistry);
        AgentRegistryClusterClient clusterClient = new JgroupsAgentRegistryClusterClient(jgroupsClusterMember.listenerMultiplexer(),
                                                                                         jgroupsClusterMember.stateMultiplexer(),
                                                                                         jgroupsClusterMember.viewMultiplexer(),
                                                                                         agentRegistry);
        clusterClient.startRegistryListener(agentRegistry.createClusterListener());
        return clusterClient;
    }

    public void close() {
        jChannel.close();
    }

    @VisibleForTesting
    public JChannel getJChannel() {
        return jChannel;
    }
}
