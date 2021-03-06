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

import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.actor.RuntimeActor;

import javax.validation.constraints.NotNull;

/**
 * Facade on an actor instance as seen by the agent hosting it.
 */
public interface ManagedActor {

    /**
     * @return the key of the actor
     */
    @NotNull
    ActorKey getKey();

    /**
     * Terminates the actor by invoking its {@link RuntimeActor#close()} method.
     */
    void close();

}
