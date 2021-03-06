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
package io.amaze.bench.cluster.leader;

import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.AgentClusterClientFactory;
import io.amaze.bench.cluster.ClusterClient;
import io.amaze.bench.cluster.agent.AgentInputMessage;

import javax.validation.constraints.NotNull;
import java.io.Closeable;

/**
 * Facade for the ResourceManager to interact with the cluster messaging system.
 *
 * @see ClusterClient
 * @see AgentClusterClientFactory
 */
public interface ResourceManagerClusterClient extends Closeable {

    /**
     * Will create an endpoint for the actor to receive messages.<br>
     * In practice a message queue can be created in the underlying implementation.<br>
     * The actor can then consume incoming messages from the queue.
     *
     * @param key The actor name, that will uniquely identify the queue in the cluster.
     */
    void initForActor(@NotNull ActorKey key);

    /**
     * Deletes the endpoint queue created for an actor to receive messages.<br>
     * The queue should have been previously created using {@link #initForActor}
     *
     * @param key The actor unique key in the cluster.
     */
    void closeForActor(@NotNull ActorKey key);

    /**
     * Will send the given message to the specified agent using the underlying messaging system.
     *
     * @param message Contents of the message, {@link AgentInputMessage}
     */
    void sendToAgent(@NotNull AgentInputMessage message);

    @Override
    void close();

}
