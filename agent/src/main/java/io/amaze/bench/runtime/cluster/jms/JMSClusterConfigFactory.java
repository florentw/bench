package io.amaze.bench.runtime.cluster.jms;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.cluster.ClusterClients;
import io.amaze.bench.runtime.cluster.ClusterConfigFactory;
import io.amaze.bench.shared.jms.JMSEndpoint;

import javax.validation.constraints.NotNull;

/**
 * JMS implementation of ClusterConfigFactory
 */
public final class JMSClusterConfigFactory implements ClusterConfigFactory {

    private final JMSEndpoint serverEndpoint;

    public JMSClusterConfigFactory(final JMSEndpoint serverEndpoint) {
        this.serverEndpoint = serverEndpoint;
    }

    @Override
    public Config clusterConfigFor(@NotNull final ActorKey actorKey) {
        return ConfigFactory.parseString("{\"" + ClusterClients.FACTORY_CLASS + "\":" + //
                                                 "\"" + JMSClusterClientFactory.class.getName() + "\"," + //
                                                 "\"" + ClusterClients.FACTORY_CONFIG + "\":" + //
                                                 "" + serverEndpoint.toConfig().root().render(ConfigRenderOptions.concise()) + "}");
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
