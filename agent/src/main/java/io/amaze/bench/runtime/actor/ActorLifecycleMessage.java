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
package io.amaze.bench.runtime.actor;

import io.amaze.bench.runtime.LifecycleMessage;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This message class represents actor lifecycle notifications.
 * It is meant to be sent by an agent to the registry.<br>
 */
public final class ActorLifecycleMessage implements LifecycleMessage {

    private final ActorKey actor;
    private final State state;
    private final String agent;
    private final ActorDeployInfo deployInfo;
    private final Throwable throwable;

    private ActorLifecycleMessage(@NotNull final State state, @NotNull final ActorKey actor,
                                  final String agent,
                                  final ActorDeployInfo deployInfo,
                                  final Throwable throwable) {
        this.actor = checkNotNull(actor);
        this.state = checkNotNull(state);
        this.agent = agent;
        this.deployInfo = deployInfo;
        this.throwable = throwable;
    }

    public static ActorLifecycleMessage created(@NotNull final ActorKey actor, @NotNull final String agent) {
        checkNotNull(actor);
        checkNotNull(agent);

        return new ActorLifecycleMessage(State.CREATED, actor, agent, null, null);
    }

    public static ActorLifecycleMessage initialized(@NotNull final ActorKey actor,
                                                    @NotNull final ActorDeployInfo deployInfo) {
        checkNotNull(actor);
        checkNotNull(deployInfo);

        return new ActorLifecycleMessage(State.INITIALIZED, actor, null, deployInfo, null);
    }

    public static ActorLifecycleMessage failed(@NotNull final ActorKey actor, @NotNull final Throwable throwable) {
        checkNotNull(actor);
        checkNotNull(throwable);

        return new ActorLifecycleMessage(State.FAILED, actor, null, null, throwable);
    }

    public static ActorLifecycleMessage closed(@NotNull final ActorKey actor) {
        checkNotNull(actor);

        return new ActorLifecycleMessage(State.CLOSED, actor, null, null, null);
    }

    @NotNull
    public ActorKey getActor() {
        return actor;
    }

    @NotNull
    public State getState() {
        return state;
    }

    public String getAgent() {
        return agent;
    }

    public ActorDeployInfo getDeployInfo() {
        return deployInfo;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return "{\"ActorLifecycleMessage\":{" + //
                "\"actor\":" + actor + ", " + //
                "\"state\":\"" + state + "\", " + //
                "\"throwable\":" + throwable + ", " + //
                "\"agent\":\"" + agent + "\", " + //
                "\"deployInfo\":" + deployInfo + "}}";
    }

    public enum State {
        CREATED, //
        INITIALIZED, //
        FAILED, //
        CLOSED //
    }
}
