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

import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.cluster.ActorClusterClient;
import io.amaze.bench.runtime.cluster.AgentClusterClient;
import io.amaze.bench.runtime.cluster.ClusterClientFactory;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.shared.jms.JMSEndpoint;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/3/16.
 */
public final class JMSClusterClientFactory implements ClusterClientFactory {

    private final JMSEndpoint endpoint;

    public JMSClusterClientFactory(@NotNull final JMSEndpoint endpoint) {
        this.endpoint = checkNotNull(endpoint);
    }

    @Override
    public AgentClusterClient createForAgent(@NotNull String agent) {
        return new JMSAgentClusterClient(endpoint, checkNotNull(agent));
    }

    @Override
    public ActorClusterClient createForActor(@NotNull ActorKey actor) {
        return new JMSActorClusterClient(endpoint, checkNotNull(actor));
    }

    @Override
    public ActorRegistryClusterClient createForActorRegistry() {
        return new JMSActorRegistryClusterClient(endpoint);
    }
}
