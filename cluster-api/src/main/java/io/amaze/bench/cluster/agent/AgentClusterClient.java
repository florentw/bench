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
package io.amaze.bench.cluster.agent;

import io.amaze.bench.cluster.ClusterClient;
import io.amaze.bench.cluster.actor.ActorRegistrySender;
import io.amaze.bench.cluster.actor.ActorSender;

import javax.validation.constraints.NotNull;

/**
 * Interface for an agent to communicate with cluster members.
 */
public interface AgentClusterClient extends ClusterClient {

    /**
     * Starts a listener for the agent to listen to incoming messages.
     *
     * @param agent    The name of the agent that will be notified of messages addressed to him on the given listener.
     * @param listener The agent's listener, to be notified of incoming messages.
     */
    void startAgentListener(@NotNull final AgentKey agent, @NotNull final AgentClientListener listener);

    @NotNull
    AgentRegistrySender agentRegistrySender();

    @NotNull
    ActorRegistrySender actorRegistrySender();

    @NotNull
    ActorSender actorSender();

}
