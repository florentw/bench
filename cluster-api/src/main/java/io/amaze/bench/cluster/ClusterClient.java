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


import io.amaze.bench.cluster.actor.RuntimeActor;

import java.io.Closeable;

/**
 * Facade to interact with the underlying messaging system for:
 * <ul>
 * <li>an Agent</li>
 * <li>or a {@link RuntimeActor})</li>
 * </ul>
 *
 * @see AgentClusterClientFactory
 * @see RuntimeActor
 */
public interface ClusterClient extends Closeable {

    /**
     * Will release resource on the underlying messaging system
     */
    @Override
    void close();
}
