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
package io.amaze.bench.orchestrator;

import io.amaze.bench.client.runtime.actor.ActorInputMessage;
import io.amaze.bench.client.runtime.agent.AgentInputMessage;
import io.amaze.bench.orchestrator.registry.ActorRegistryListener;
import io.amaze.bench.orchestrator.registry.AgentRegistryListener;

import javax.validation.constraints.NotNull;

/**
 * Facade to interact with the underlying messaging system for the orchestration server.
 *
 * @see io.amaze.bench.client.runtime.orchestrator.OrchestratorClient
 * @see io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory
 */
interface OrchestratorServer extends AutoCloseable {

    /**
     * Register the given listeners to be plugged to the underlying message system.<br>
     * <ul>
     * <li>{@link AgentRegistryListener} will then be notified of Agent related events</li>
     * <li>{@link ActorRegistryListener} will be notified of Actor related events</li>
     * </ul>
     *
     * @param agentsListener Listener that will be called upon agents notifications.
     * @param actorsListener Listener that will be called upon actors notifications.
     */
    void startRegistryListeners(@NotNull AgentRegistryListener agentsListener,
                                @NotNull ActorRegistryListener actorsListener);

    /**
     * Will create an endpoint for the actor to receive messages.<br>
     * In practice a message queue is created in the underlying implementation.<br>
     * The actor can then consume incoming messages from the queue.
     *
     * @param actorName The actor name, that will uniquely identify the queue in the system.
     */
    void createActorQueue(@NotNull String actorName);

    /**
     * Deletes the endpoint queue created for an actor to receive messages.<br>
     * The queue should have been previously created using {@link #createActorQueue}
     *
     * @param actorName The actor unique name.
     */
    void deleteActorQueue(@NotNull String actorName);

    /**
     * Will send the given message to the specified agent using the underlying messaging system.
     *
     * @param message Contents of the message, {@link AgentInputMessage}
     */
    void sendToAgent(@NotNull AgentInputMessage message);

    /**
     * Will send the given message to the specified actor using the underlying messaging system.
     *
     * @param actorName The actor name to send the message to
     * @param message   Contents of the message, {@link ActorInputMessage}
     */
    void sendToActor(@NotNull String actorName, @NotNull ActorInputMessage message);

    /**
     * Will release resources on the underlying messaging system.
     */
    @Override
    void close();

}
