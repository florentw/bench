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
package io.amaze.bench.cluster;

import com.typesafe.config.Config;
import io.amaze.bench.cluster.registry.ActorRegistry;
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

    public static <T> T newFactory(@NotNull final Class<T> clusterClientInterface,
                                   @NotNull final Config clusterConfig,
                                   @NotNull final ActorRegistry actorRegistry) {

        checkNotNull(clusterClientInterface);
        checkNotNull(clusterConfig);
        checkNotNull(actorRegistry);

        String factoryClassName = clusterConfig.getString(FACTORY_CLASS);
        Config factoryConfig = clusterConfig.getConfig(FACTORY_CONFIG);

        MutablePicoContainer container = new DefaultPicoContainer();
        container.addComponent(factoryConfig);
        container.addComponent(actorRegistry);

        try {
            Class<? extends T> factoryClass = Class.forName(factoryClassName) // NOSONAR
                    .asSubclass(clusterClientInterface); // NOSONAR
            container.addComponent(factoryClass);
            return container.getComponent(factoryClass);
        } catch (ClassNotFoundException e) {
            throw propagate(e);
        }
    }
}
