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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.amaze.bench.cluster.ClusterClients;
import io.amaze.bench.cluster.ClusterConfigFactory;
import io.amaze.bench.cluster.actor.ActorKey;

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
