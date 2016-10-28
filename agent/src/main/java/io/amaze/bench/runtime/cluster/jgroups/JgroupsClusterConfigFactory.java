package io.amaze.bench.runtime.cluster.jgroups;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.cluster.ClusterClients;
import io.amaze.bench.runtime.cluster.ClusterConfigFactory;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Jgroups implementation of ClusterConfigFactory
 */
public final class JgroupsClusterConfigFactory implements ClusterConfigFactory {

    private final Config jgroupsFactoryConfig;

    public JgroupsClusterConfigFactory(final Config jgroupsFactoryConfig) {
        this.jgroupsFactoryConfig = checkNotNull(jgroupsFactoryConfig);
    }

    @Override
    public Config clusterConfigFor(@NotNull final ActorKey actorKey) {
        return ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":" + //
                                                 "\"" + JgroupsClusterClientFactory.class.getName() + "\"," + //
                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":" + //
                                                 jgroupsFactoryConfig.root().render(ConfigRenderOptions.concise()) + "}");
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
