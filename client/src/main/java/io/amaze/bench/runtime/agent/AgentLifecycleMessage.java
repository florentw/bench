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
package io.amaze.bench.runtime.agent;

import io.amaze.bench.runtime.LifecycleMessage;
import io.amaze.bench.runtime.actor.ActorLifecycleMessage;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Message that notifies the Agent registries of an {@link Agent} lifecycle change.
 *
 * @see Agent
 * @see ActorLifecycleMessage
 * @see AgentRegistrationMessage
 */
public final class AgentLifecycleMessage implements LifecycleMessage {

    private final State state;
    private final String agent;
    private final AgentRegistrationMessage registrationMessage;

    private AgentLifecycleMessage(@NotNull final State state,
                                  @NotNull final String agent,
                                  final AgentRegistrationMessage registrationMessage) {
        this.state = checkNotNull(state);
        this.agent = checkNotNull(agent);
        this.registrationMessage = registrationMessage;
    }

    /**
     * Creates an instance of {@link AgentLifecycleMessage} to notify of the agent creation.
     *
     * @param registrationMessage Registration information for this agent.
     * @return A non-{@code null} instance of {@link AgentLifecycleMessage}
     */
    public static AgentLifecycleMessage created(@NotNull final AgentRegistrationMessage registrationMessage) {
        checkNotNull(registrationMessage);
        return new AgentLifecycleMessage(State.CREATED, registrationMessage.getName(), registrationMessage);
    }

    /**
     * Creates an instance of {@link AgentLifecycleMessage} to notify of the agent termination.
     *
     * @param agent The agent name.
     * @return A non-{@code null} instance of {@link AgentLifecycleMessage}
     */
    public static AgentLifecycleMessage closed(@NotNull final String agent) {
        checkNotNull(agent);
        return new AgentLifecycleMessage(State.CLOSED, agent, null);
    }

    /**
     * @return An enum describing the data serialized along as payload.
     * @see State for more information.
     */
    public State getState() {
        return state;
    }

    /**
     * @return The {@link AgentRegistrationMessage} instance or {@code null}.
     */
    public AgentRegistrationMessage getRegistrationMessage() {
        return registrationMessage;
    }

    /**
     * @return The agent name.
     */
    public String getAgent() {
        return agent;
    }

    /**
     * State that can be taken by agent:
     * <ul>
     * <li>CREATED: Started and notifies the cluster of its availability</li>
     * <li>CLOSED: Stopped and notifies the cluster of its unavailability</li>
     * </ul>
     */
    public enum State {
        CREATED, //
        CLOSED,
    }
}
