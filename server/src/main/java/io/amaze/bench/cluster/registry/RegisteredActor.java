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
package io.amaze.bench.cluster.registry;

import io.amaze.bench.client.runtime.actor.ActorDeployInfo;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/29/16.
 */
public final class RegisteredActor {

    private final String actor;
    private final String agent;
    private final State state;
    private final ActorDeployInfo deployInfo;

    RegisteredActor(@NotNull final String actor, @NotNull final String agent, @NotNull final State state) {
        this(actor, agent, state, null);
    }

    RegisteredActor(@NotNull final String actor,
                    @NotNull final String agent,
                    @NotNull final State state, final ActorDeployInfo deployInfo) {
        this.actor = checkNotNull(actor);
        this.agent = checkNotNull(agent);
        this.state = checkNotNull(state);
        this.deployInfo = deployInfo;
    }

    @NotNull
    public String getName() {
        return actor;
    }

    @NotNull
    public State getState() {
        return state;
    }

    @NotNull
    public String getAgent() {
        return agent;
    }

    public ActorDeployInfo getDeployInfo() {
        return deployInfo;
    }

    public enum State {
        CREATED, //
        INITIALIZED //
    }
}
