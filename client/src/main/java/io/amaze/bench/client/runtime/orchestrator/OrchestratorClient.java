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
package io.amaze.bench.client.runtime.orchestrator;


import io.amaze.bench.client.runtime.actor.BaseActor;
import io.amaze.bench.client.runtime.agent.Agent;
import io.amaze.bench.client.runtime.message.Message;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Facade to interact with the underlying messaging system for an orchestration client (an {@link Agent}).<br>
 * The alter-ego of this interface is OrchestratorServer on the orchestration server side.
 *
 * @see io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory
 * @see Agent
 * @see BaseActor
 */
public interface OrchestratorClient extends AutoCloseable {

    /**
     * A call to this method will send a message {@code message} to the target actor {@code to}
     * using the underlying messaging system.
     *
     * @param to      Target actor name
     * @param message Payload to send
     */
    void sendToActor(@NotNull final String to, @NotNull final Message<? extends Serializable> message);

    /**
     * Will release resource on the underlying messaging system
     */
    @Override
    void close();
}
