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
package io.amaze.bench.cluster.registry;

import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.actor.ActorDeployInfo;
import io.amaze.bench.cluster.agent.AgentKey;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/28/16.
 */
public interface ActorRegistryListener {

    void onActorCreated(@NotNull ActorKey actorKey, @NotNull AgentKey agent);

    void onActorInitialized(@NotNull ActorKey actorKey, @NotNull ActorDeployInfo deployInfo);

    void onActorFailed(@NotNull ActorKey actorKey, @NotNull Throwable throwable);

    void onActorClosed(@NotNull ActorKey actorKey);
}
