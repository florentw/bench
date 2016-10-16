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


import io.amaze.bench.runtime.actor.ActorInternal;
import io.amaze.bench.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.runtime.actor.RuntimeActor;
import io.amaze.bench.runtime.agent.Agent;

import javax.validation.constraints.NotNull;

/**
 * Facade to interact with the underlying messaging system for:
 * <ul>
 * <li>an {@link Agent}</li>
 * <li>or a {@link RuntimeActor})</li>
 * </ul>
 *
 * @see ClusterClientFactory
 * @see Agent
 * @see ActorSender
 * @see ActorInternal
 */
public interface ClusterClient extends AutoCloseable {

    /**
     * A call to this method will send a message {@code message} to the actor registry topic
     * using the underlying messaging system.
     *
     * @param actorLifecycleMessage Message to send to the topic
     */
    void sendToActorRegistry(@NotNull final ActorLifecycleMessage actorLifecycleMessage);

    /**
     * Will release resource on the underlying messaging system
     */
    @Override
    void close();
}
