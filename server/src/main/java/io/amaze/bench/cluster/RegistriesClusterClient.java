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
package io.amaze.bench.cluster;

import io.amaze.bench.cluster.registry.ActorRegistryListener;
import io.amaze.bench.cluster.registry.AgentRegistryListener;

import javax.validation.constraints.NotNull;

/**
 * Facade for the registries to interact with the cluster messaging system.
 *
 * @see AgentRegistryListener
 * @see ActorRegistryListener
 */
@FunctionalInterface
public interface RegistriesClusterClient {

    /**
     * Register the given listeners to be plugged to the underlying message system.<br>
     * <ul>
     * <li>{@link AgentRegistryListener} will then be notified of Agent related events</li>
     * <li>{@link ActorRegistryListener} will be notified of Actor related events</li>
     * </ul>
     *
     * @param agentsListener Listener that will be called upon agents notifications.
     * @param actorsListener Listener that will be called upon actors notifications.
     */
    void startRegistryListeners(@NotNull AgentRegistryListener agentsListener,
                                @NotNull ActorRegistryListener actorsListener);

}
