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
package io.amaze.bench.cluster.actor;

import io.amaze.bench.cluster.ClusterClient;
import io.amaze.bench.cluster.Endpoint;
import io.amaze.bench.cluster.metric.MetricValuesMessage;

import javax.validation.constraints.NotNull;

/**
 * Interface for an actor to communicate with cluster members.
 */
public interface ActorClusterClient extends ClusterClient {

    /**
     * Starts a listener for the specified actor name to listen to incoming messages.
     *
     * @param actor The name of the agent that will be notified of messages addressed to it on the given listener.
     */
    void startActorListener(@NotNull final RuntimeActor actor);

    /**
     * A call to this method will send metrics {@code message} to the metrics topic.
     *
     * @param message Payload to send
     */
    void sendMetrics(@NotNull final MetricValuesMessage message);

    /**
     * @return Local endpoint to be used by the cluster layer to discuss with this actor.
     */
    @NotNull
    Endpoint localEndpoint();

    /**
     * Allows a client to send messages to an actor instance in the cluster.
     *
     * @return A non null instance of {@link ActorSender}
     */
    @NotNull
    ActorSender actorSender();

    /**
     * To be used by the actor to broadcast lifecycle events.
     */
    @NotNull
    ActorRegistrySender actorRegistrySender();

}
