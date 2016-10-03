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
package io.amaze.bench.runtime.cluster;

import io.amaze.bench.runtime.actor.RuntimeActor;
import io.amaze.bench.runtime.actor.metric.MetricValuesMessage;

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

}
