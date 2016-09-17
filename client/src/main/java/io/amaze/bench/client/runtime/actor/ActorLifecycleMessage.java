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
package io.amaze.bench.client.runtime.actor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This message class represents actor lifecycle notifications.<br>
 * It is meant to be sent by an agent to the master.<br>
 * <p>
 * Created on 3/6/16.
 */
public final class ActorLifecycleMessage implements Serializable {

    private final String actor;
    private final Phase phase;
    private final String agent;
    private final ActorDeployInfo deployInfo;
    private final Throwable throwable;

    private ActorLifecycleMessage(@NotNull final Phase phase,
                                  @NotNull final String actor,
                                  final String agent,
                                  final ActorDeployInfo deployInfo,
                                  final Throwable throwable) {
        this.actor = checkNotNull(actor);
        this.phase = checkNotNull(phase);
        this.agent = agent;
        this.deployInfo = deployInfo;
        this.throwable = throwable;
    }

    public static ActorLifecycleMessage created(@NotNull final String actor, @NotNull final String agent) {
        checkNotNull(actor);
        checkNotNull(agent);

        return new ActorLifecycleMessage(Phase.CREATED, actor, agent, null, null);
    }

    public static ActorLifecycleMessage initialized(@NotNull final String actor,
                                                    @NotNull final ActorDeployInfo deployInfo) {
        checkNotNull(actor);
        checkNotNull(deployInfo);

        return new ActorLifecycleMessage(Phase.INITIALIZED, actor, null, deployInfo, null);
    }

    public static ActorLifecycleMessage failed(@NotNull final String actor, @NotNull final Throwable throwable) {
        checkNotNull(actor);
        checkNotNull(throwable);

        return new ActorLifecycleMessage(Phase.FAILED, actor, null, null, throwable);
    }

    public static ActorLifecycleMessage closed(@NotNull final String actor) {
        checkNotNull(actor);

        return new ActorLifecycleMessage(Phase.CLOSED, actor, null, null, null);
    }

    @NotNull
    public String getActor() {
        return actor;
    }

    @NotNull
    public Phase getPhase() {
        return phase;
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
                "\"actor\":\"" + actor + "\"" + ", " + //
                "\"phase\":\"" + phase + "\"" + ", " + //
                "\"throwable\":" + throwable + ", " + //
                "\"agent\":\"" + agent + "\"" + ", " + //
                "\"deployInfo\":" + deployInfo + "}}";
    }

    public enum Phase {
        CREATED, //
        INITIALIZED, //
        FAILED, //
        CLOSED //
    }
}
