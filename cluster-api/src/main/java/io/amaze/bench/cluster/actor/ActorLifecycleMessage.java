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
package io.amaze.bench.cluster.actor;

import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.LifecycleMessage;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.registry.ActorRegistryListener;

import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * This message class represents actor lifecycle notifications.
 * It is meant to be sent by an agent to the registry.<br>
 */
public final class ActorLifecycleMessage implements LifecycleMessage {

    private final ActorKey actor;
    private final State state;
    private final AgentKey agent;
    private final ActorDeployInfo deployInfo;
    private final Throwable throwable;

    private ActorLifecycleMessage(@NotNull final State state,
                                  @NotNull final ActorKey actor,
                                  final AgentKey agent,
                                  final ActorDeployInfo deployInfo,
                                  final Throwable throwable) {
        this.actor = requireNonNull(actor);
        this.state = requireNonNull(state);
        this.agent = agent;
        this.deployInfo = deployInfo;
        this.throwable = throwable;
    }

    public static ActorLifecycleMessage created(@NotNull final ActorKey actor, @NotNull final AgentKey agent) {
        requireNonNull(actor);
        requireNonNull(agent);

        return new ActorLifecycleMessage(State.CREATED, actor, agent, null, null);
    }

    public static ActorLifecycleMessage initialized(@NotNull final ActorKey actor,
                                                    @NotNull final ActorDeployInfo deployInfo) {
        requireNonNull(actor);
        requireNonNull(deployInfo);

        return new ActorLifecycleMessage(State.INITIALIZED, actor, null, deployInfo, null);
    }

    public static ActorLifecycleMessage failed(@NotNull final ActorKey actor, @NotNull final Throwable throwable) {
        requireNonNull(actor);
        requireNonNull(throwable);

        return new ActorLifecycleMessage(State.FAILED, actor, null, null, throwable);
    }

    public static ActorLifecycleMessage closed(@NotNull final ActorKey actor) {
        requireNonNull(actor);

        return new ActorLifecycleMessage(State.CLOSED, actor, null, null, null);
    }

    /**
     * Calls the given registry listener using the current message.
     * Not synchronized.
     *
     * @param registryListener Listener to be notified with the event represented by this message.
     */
    public void sendTo(final ActorRegistryListener registryListener) {
        requireNonNull(registryListener);

        switch (getState()) {
            case CREATED:
                registryListener.onActorCreated(actor, agent);
                break;
            case INITIALIZED:
                registryListener.onActorInitialized(actor, deployInfo);
                break;
            case FAILED:
                registryListener.onActorFailed(actor, throwable);
                break;
            case CLOSED:
                registryListener.onActorClosed(actor);
                break;
            default:
        }
    }

    @NotNull
    public ActorKey getActor() {
        return actor;
    }

    @NotNull
    public State getState() {
        return state;
    }

    public AgentKey getAgent() {
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
                "\"agent\":" + agent + ", " + //
                "\"deployInfo\":" + deployInfo + "}}";
    }

    public enum State {
        CREATED, //
        INITIALIZED, //
        FAILED, //
        CLOSED //
    }
}
