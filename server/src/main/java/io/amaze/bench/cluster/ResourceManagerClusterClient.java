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

import io.amaze.bench.client.runtime.agent.AgentInputMessage;
import io.amaze.bench.client.runtime.cluster.ClusterClient;
import io.amaze.bench.client.runtime.cluster.ClusterClientFactory;

import javax.validation.constraints.NotNull;

/**
 * Facade for the {@link ResourceManager} to interact with the cluster messaging system.
 *
 * @see ClusterClient
 * @see ClusterClientFactory
 */
public interface ResourceManagerClusterClient {

    /**
     * Will create an endpoint for the actor to receive messages.<br>
     * In practice a message queue is created in the underlying implementation.<br>
     * The actor can then consume incoming messages from the queue.
     *
     * @param actorName The actor name, that will uniquely identify the queue in the system.
     */
    void initForActor(@NotNull String actorName);

    /**
     * Deletes the endpoint queue created for an actor to receive messages.<br>
     * The queue should have been previously created using {@link #initForActor}
     *
     * @param actorName The actor unique name.
     */
    void closeForActor(@NotNull String actorName);

    /**
     * Will send the given message to the specified agent using the underlying messaging system.
     *
     * @param message Contents of the message, {@link AgentInputMessage}
     */
    void sendToAgent(@NotNull AgentInputMessage message);

}
