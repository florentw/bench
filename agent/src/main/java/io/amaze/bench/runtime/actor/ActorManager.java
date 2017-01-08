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
package io.amaze.bench.runtime.actor;


import io.amaze.bench.cluster.actor.ActorConfig;
import io.amaze.bench.cluster.actor.DeployConfig;
import io.amaze.bench.cluster.actor.ValidationException;
import io.amaze.bench.runtime.agent.Agent;

import javax.validation.constraints.NotNull;

/**
 * Responsible of the lifecycle of actors as viewed from the point of view of the
 * {@link Agent}.
 */
public interface ActorManager extends AutoCloseable {

    /**
     * Creates an instance of actor with using the provided {@link ActorConfig}.
     *
     * @param actorConfig Configuration of the actor to be used by the {@link ActorManager},
     *                    contains the {@link DeployConfig} and the json configuration to be passed to instance.
     * @return An instance of {@link ManagedActor} that represents a handle on a actor, and can be used to kill it.
     * @throws ValidationException If the actor is invalid.
     */
    @NotNull
    ManagedActor createActor(@NotNull ActorConfig actorConfig) throws ValidationException;

    /**
     * Release resources handled by the {@link ActorManager}.
     */
    @Override
    void close();

}
