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
package io.amaze.bench.leader.cluster.registry;

import io.amaze.bench.runtime.agent.Agent;

import javax.validation.constraints.NotNull;

/**
 * Facade to add a listener on {@link Agent} lifecycle events.
 *
 * @see AgentRegistryListener
 */
@FunctionalInterface
public interface AgentRegistryClusterClient {

    /**
     * Register the given listeners to be plugged to the underlying message system.
     * {@link AgentRegistryListener} instance will be notified of {@link Agent} related events.
     *
     * @param agentsListener Listener that will be called upon actors notifications.
     */
    void startRegistryListener(@NotNull AgentRegistryListener agentsListener);

}
