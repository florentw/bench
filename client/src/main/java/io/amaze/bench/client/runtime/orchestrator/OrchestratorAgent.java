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

import io.amaze.bench.client.runtime.agent.AgentClientListener;

import javax.validation.constraints.NotNull;

/**
 * Created on 4/24/16.
 */
public interface OrchestratorAgent extends OrchestratorClient {

    /**
     * Starts a listener for the agent to listen to incoming messages.
     *
     * @param agent    The name of the agent that will be notified of messages addressed to him on the given listener.
     * @param listener An listener for the agent to be notified of incoming messages.
     */
    void startAgentListener(@NotNull final String agent, @NotNull final AgentClientListener listener);

}
