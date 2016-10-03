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
package io.amaze.bench.runtime.actor;

import io.amaze.bench.runtime.cluster.ActorClusterClient;
import io.amaze.bench.runtime.cluster.ClusterClientFactory;
import io.amaze.bench.shared.jms.JMSEndpoint;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * /**
 * Factory to create flavors of {@link ActorManager}:
 * <ul>
 * <li>{@link EmbeddedActorManager} a manager that will spawn instances of actor inside the current JVM.</li>
 * <li>{@link ForkedActorManager} will fork a new JVM to spawn each actor.</li>
 * </ul>
 *
 * @see ActorManager
 */
public class ActorManagers {

    private final JMSEndpoint masterEndpoint;

    public ActorManagers(final JMSEndpoint masterEndpoint) {
        this.masterEndpoint = checkNotNull(masterEndpoint);
    }

    /**
     * Will create a new instance of {@link ActorManager} that will instantiate actors in the current JVM.
     *
     * @param agentName The host agent name.
     * @param factory   An {@link ClusterClientFactory} to be used to create
     *                  {@link ActorClusterClient} instances.
     * @return An instantiated {@link EmbeddedActorManager}
     */
    @NotNull
    public ActorManager createEmbedded(@NotNull final String agentName, @NotNull final ClusterClientFactory factory) {
        checkNotNull(agentName);
        checkNotNull(factory);

        return new EmbeddedActorManager(agentName, new Actors(factory));
    }

    /**
     * Will create a new instance of {@link ActorManager} that will instantiate each actor in a new JVM.
     *
     * @param agentName The host agent name.
     * @return An instantiated {@link ForkedActorManager}
     */
    @NotNull
    public ActorManager createForked(@NotNull final String agentName) {
        checkNotNull(agentName);

        return new ForkedActorManager(agentName, masterEndpoint);
    }

}