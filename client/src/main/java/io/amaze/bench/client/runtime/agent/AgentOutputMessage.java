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

import io.amaze.bench.client.runtime.actor.ActorLifecycleMessage;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a message sent to the OrchestratorServer master from an {@link Agent}.<br/>
 * Created on 3/3/16.
 *
 * @see Agent
 * @see ActorLifecycleMessage
 * @see AgentRegistrationMessage
 */
public final class AgentOutputMessage implements java.io.Serializable {

    private final Action action;
    private final Serializable data;

    public AgentOutputMessage(@NotNull final Action action, @NotNull final Serializable data) {
        this.action = checkNotNull(action);
        this.data = checkNotNull(data);
    }

    /**
     * @return An enum describing the data serialized along as payload.
     * @see Action for more information
     */
    public Action getAction() {
        return action;
    }

    /**
     * @return The serialized payload of which type is described by the {@link Action} enum carried.
     * @see Action for more information
     */
    public Serializable getData() {
        return data;
    }

    /**
     * Types of the object returned by {@link #getData()} when the value is:
     * <li>
     * <li>{@link #REGISTER_AGENT}: A {@link AgentRegistrationMessage} is returned
     * <li>{@link #UNREGISTER_AGENT}: A {@link String} is returned (the name of the agent signing off).</li>
     * <li>{@link #ACTOR_LIFECYCLE}: A {@link ActorLifecycleMessage}</li>
     * </ul>
     */
    public enum Action {
        REGISTER_AGENT, //
        UNREGISTER_AGENT, //
        ACTOR_LIFECYCLE //
    }

}
