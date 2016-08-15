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
package io.amaze.bench.client.runtime.agent;

import io.amaze.bench.client.runtime.actor.ActorConfig;

import javax.validation.constraints.NotNull;

/**
 * Interface to be implemented by an agent. These hooks are called upon reception of messages from the master.
 * All these methods' implementations should fail silently.
 */
public interface AgentClientListener {

    /**
     * Method is called when an actor creation request is received from the master.
     *
     * @param actorConfig The actor configuration
     */
    void onActorCreationRequest(@NotNull ActorConfig actorConfig);

    /**
     * Called when the agent is requested to close an actor.
     *
     * @param actor Actor to close
     */
    void onActorCloseRequest(@NotNull String actor);
}