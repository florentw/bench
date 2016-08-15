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

import javax.validation.constraints.NotNull;

/**
 * Factory for interfaces to interact with orchestration layer.
 * <ul>
 * <li>Provides {@link OrchestratorAgent} instances for the {@link io.amaze.bench.client.runtime.agent.Agent}
 * to listen to incoming messages.</li>
 * <li>Provides {@link OrchestratorActor} instances for actors to listen to incoming messages
 * and be able to send back.</li>
 * </ul>
 *
 * @see OrchestratorAgent
 * @see OrchestratorActor
 */
public interface OrchestratorClientFactory {

    @NotNull
    OrchestratorAgent createForAgent();

    @NotNull
    OrchestratorActor createForActor();

}
