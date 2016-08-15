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
package io.amaze.bench.orchestrator;

import io.amaze.bench.client.runtime.actor.ActorConfig;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/9/16.
 */
public interface ResourceManager extends AutoCloseable {

    /**
     * Requests an actor instantiation using the given {@link ActorConfig} configuration.
     *
     * @param actorConfig The actor configuration.
     * @throws IllegalStateException if the agent cannot be created before
     */
    void createActor(@NotNull ActorConfig actorConfig);

    /**
     * Requests to close an actor. It will contact the agent hosting the actor in order for it to close it.
     *
     * @param name The name of the actor to close.
     * @throws IllegalArgumentException if the actor does not exist.
     */
    void closeActor(@NotNull String name);

    /**
     * Closes every actor handled by this ResourceManager
     */
    @Override
    void close();

}
