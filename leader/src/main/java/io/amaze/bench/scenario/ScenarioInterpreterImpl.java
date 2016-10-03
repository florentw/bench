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

package io.amaze.bench.scenario;

import io.amaze.bench.leader.cluster.ResourceManager;
import io.amaze.bench.leader.cluster.registry.AgentRegistry;
import io.amaze.bench.runtime.actor.ActorConfig;
import io.amaze.bench.runtime.actor.ActorDeployInfo;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryListener;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 4/17/16.
 */
public class ScenarioInterpreterImpl implements ScenarioInterpreter {

    private final ResourceManager resourceManager;
    private final AgentRegistry agentRegistry;
    private final ActorRegistry actorRegistry;

    public ScenarioInterpreterImpl(@NotNull final ResourceManager resourceManager,
                                   @NotNull final AgentRegistry agentRegistry,
                                   @NotNull final ActorRegistry actorRegistry) {

        this.resourceManager = checkNotNull(resourceManager);
        this.agentRegistry = checkNotNull(agentRegistry);
        this.actorRegistry = checkNotNull(actorRegistry);
    }

    @Override
    public void runScenario(@NotNull final Scenario scenario) {

        for (ActorConfig actorConf : scenario.getActors()) {
            resourceManager.createActor(actorConf);
        }

        actorRegistry.addListener(new ActorRegistryListener() {
            @Override
            public void onActorCreated(@NotNull final ActorKey name, @NotNull final String agent) {

            }

            @Override
            public void onActorInitialized(@NotNull final ActorKey name, @NotNull final ActorDeployInfo deployInfo) {

            }

            @Override
            public void onActorFailed(@NotNull final ActorKey name, @NotNull final Throwable throwable) {

            }

            @Override
            public void onActorClosed(@NotNull final ActorKey name) {

            }
        });

        // State 1 -> Init all actors
        // Barrier


        // State 2 -> Start all actors

        // Wait for actors to finish
        // or elapsed time + stop all

    }
}
