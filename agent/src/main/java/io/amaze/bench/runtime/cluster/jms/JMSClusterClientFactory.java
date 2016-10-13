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
package io.amaze.bench.runtime.cluster.jms;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;
import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.cluster.ActorClusterClient;
import io.amaze.bench.runtime.cluster.AgentClusterClient;
import io.amaze.bench.runtime.cluster.ClusterClientFactory;
import io.amaze.bench.runtime.cluster.ClusterConfigFactory;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.shared.jms.JMSEndpoint;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/3/16.
 */
public final class JMSClusterClientFactory implements ClusterClientFactory {

    private static final JMSEndpoint DUMMY_JMS_ENDPOINT = new JMSEndpoint("dummy", 1337);
    private final JMSEndpoint serverEndpoint;

    public JMSClusterClientFactory(@NotNull final Config clusterConfig) {
        checkNotNull(clusterConfig);
        this.serverEndpoint = new JMSEndpoint(clusterConfig);
    }

    @VisibleForTesting
    JMSClusterClientFactory(@NotNull final JMSEndpoint serverEndpoint) {
        this.serverEndpoint = checkNotNull(serverEndpoint);
    }

    public Endpoint getLocalEndpoint() {
        return DUMMY_JMS_ENDPOINT;
    }

    @Override
    public AgentClusterClient createForAgent(@NotNull String agent) {
        return new JMSAgentClusterClient(serverEndpoint, checkNotNull(agent));
    }

    @Override
    public ActorClusterClient createForActor(@NotNull ActorKey actor) {
        return new JMSActorClusterClient(serverEndpoint, checkNotNull(actor));
    }

    @Override
    public ActorRegistryClusterClient createForActorRegistry() {
        return new JMSActorRegistryClusterClient(serverEndpoint);
    }

    @Override
    public ClusterConfigFactory clusterConfigFactory() {
        return new JMSClusterConfigFactory(serverEndpoint);
    }

}

