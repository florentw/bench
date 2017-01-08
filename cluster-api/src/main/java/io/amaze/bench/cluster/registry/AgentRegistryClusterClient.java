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
package io.amaze.bench.cluster.registry;

import javax.validation.constraints.NotNull;
import java.io.Closeable;

/**
 * Facade to add a listener on agent lifecycle events.
 *
 * @see AgentRegistryListener
 */
public interface AgentRegistryClusterClient extends Closeable {

    /**
     * Register the given listeners to be plugged to the underlying message system.
     * {@link AgentRegistryListener} instance will be notified of agent related events.
     *
     * @param agentsListener Listener that will be called upon actors notifications.
     */
    void startRegistryListener(@NotNull AgentRegistryListener agentsListener);

    @Override
    void close();

}
