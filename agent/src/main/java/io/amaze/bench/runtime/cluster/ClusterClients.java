package io.amaze.bench.runtime.cluster;

import com.google.common.base.Throwables;
import com.typesafe.config.Config;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/10/16.
 */
public final class ClusterClients {

    public static final String FACTORY_CLASS = "factoryClass";
    public static final String FACTORY_CONFIG = "factoryConfig";

    private ClusterClients() {
        // Should not be instantiated
    }

    public static ClusterClientFactory newFactory(@NotNull final Config clusterConfig) {
        checkNotNull(clusterConfig);
        String factoryClassName = clusterConfig.getString(FACTORY_CLASS);
        Config factoryConfig = clusterConfig.getConfig(FACTORY_CONFIG);

        try {
            Class<? extends ClusterClientFactory> factoryClass = Class.forName(factoryClassName).asSubclass(
                    ClusterClientFactory.class);
            return newFactory(factoryClass, factoryConfig);
        } catch (ClassNotFoundException e) {
            throw Throwables.propagate(e);
        }
    }

    private static ClusterClientFactory newFactory(@NotNull final Class<? extends ClusterClientFactory> factoryClass,
                                                   @NotNull final Config clusterConfig) {
        try {
            Constructor<? extends ClusterClientFactory> constructor = factoryClass.getConstructor(Config.class);
            return constructor.newInstance(clusterConfig);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            throw Throwables.propagate(e);
        }
    }

}
