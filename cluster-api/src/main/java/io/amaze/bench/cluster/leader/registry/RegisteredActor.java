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
package io.amaze.bench.cluster.leader.registry;

import io.amaze.bench.cluster.actor.ActorDeployInfo;
import io.amaze.bench.cluster.actor.ActorKey;
import io.amaze.bench.cluster.agent.AgentKey;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/29/16.
 */
public final class RegisteredActor implements Serializable {

    private final ActorKey actor;
    private final AgentKey agent;
    private final State state;
    private final ActorDeployInfo deployInfo;

    private RegisteredActor(@NotNull final ActorKey actor, //
                            @NotNull final AgentKey agent, //
                            @NotNull final State state, //
                            final ActorDeployInfo deployInfo) {

        this.actor = checkNotNull(actor);
        this.agent = checkNotNull(agent);
        this.state = checkNotNull(state);
        this.deployInfo = deployInfo;
    }

    public static RegisteredActor created(@NotNull final ActorKey actor, //
                                          @NotNull final AgentKey agent) {
        return new RegisteredActor(actor, agent, State.CREATED, null);
    }

    public static RegisteredActor initialized(@NotNull RegisteredActor created, //
                                              @NotNull final ActorDeployInfo deployInfo) {
        checkNotNull(created);
        checkNotNull(deployInfo);
        return new RegisteredActor(created.getKey(), created.getAgent(), State.INITIALIZED, deployInfo);
    }

    @NotNull
    public ActorKey getKey() {
        return actor;
    }

    @NotNull
    public State getState() {
        return state;
    }

    @NotNull
    public AgentKey getAgent() {
        return agent;
    }

    public ActorDeployInfo getDeployInfo() {
        return deployInfo;
    }

    @Override
    public String toString() {
        return "{\"RegisteredActor\":{" + //
                "\"actor\":" + actor + ", " + //
                "\"agent\":" + agent + ", " + //
                "\"state\":\"" + state + "\"" + ", " + //
                "\"deployInfo\":" + deployInfo + "}}";
    }

    public enum State {
        CREATED, //
        INITIALIZED //
    }
}
