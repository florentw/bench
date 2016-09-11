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
    private final Throwable throwable;
    private final String agent;

    public ActorLifecycleMessage(@NotNull final String actor, @NotNull final String agent, @NotNull final Phase phase) {
        this(actor, agent, phase, null);
    }

    public ActorLifecycleMessage(@NotNull final String actor,
                                 @NotNull final String agent,
                                 @NotNull final Phase phase,
                                 final Throwable throwable) {
        this.actor = checkNotNull(actor);
        this.agent = checkNotNull(agent);
        this.phase = checkNotNull(phase);
        this.throwable = throwable;
    }

    @NotNull
    public String getActor() {
        return actor;
    }

    @NotNull
    public Phase getPhase() {
        return phase;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public String getAgent() {
        return agent;
    }

    public enum Phase {
        CREATED, //
        INITIALIZED, //
        FAILED, //
        CLOSED //
    }
}
