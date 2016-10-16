package io.amaze.bench.runtime.cluster;

import com.typesafe.config.Config;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

/**
 * Created on 10/10/16.
 */
public final class ClusterClients {

    public static final String FACTORY_CLASS = "factoryClass";
    public static final String FACTORY_CONFIG = "factoryConfig";

    private ClusterClients() {
        // Should not be instantiated
    }

    public static ClusterClientFactory newFactory(@NotNull final Config clusterConfig,
                                                  @NotNull final ActorRegistry actorRegistry) {
        checkNotNull(clusterConfig);
        checkNotNull(actorRegistry);

        String factoryClassName = clusterConfig.getString(FACTORY_CLASS);
        Config factoryConfig = clusterConfig.getConfig(FACTORY_CONFIG);

        MutablePicoContainer container = new DefaultPicoContainer();
        container.addComponent(factoryConfig);
        container.addComponent(actorRegistry);

        try {
            Class<? extends ClusterClientFactory> factoryClass = Class.forName(factoryClassName).asSubclass(
                    ClusterClientFactory.class);
            container.addComponent(factoryClass);
            return container.getComponent(factoryClass);
        } catch (ClassNotFoundException e) {
            throw propagate(e);
        }
    }
}
