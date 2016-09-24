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
package io.amaze.bench.cluster;

import com.google.common.util.concurrent.SettableFuture;
import io.amaze.bench.client.runtime.actor.ActorManagers;
import io.amaze.bench.client.runtime.agent.Agent;
import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import io.amaze.bench.client.runtime.cluster.ClusterClientFactory;
import io.amaze.bench.cluster.registry.AgentRegistry;
import io.amaze.bench.cluster.registry.AgentRegistryListener;

import javax.validation.constraints.NotNull;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 9/12/16.
 */
public final class Agents {

    private final ActorManagers actorManagers;
    private final ClusterClientFactory clientFactory;
    private final AgentRegistry agentRegistry;

    public Agents(@NotNull final ActorManagers actorManagers, @NotNull final ClusterClientFactory clientFactory,
                  @NotNull final AgentRegistry agentRegistry) {
        this.actorManagers = checkNotNull(actorManagers);
        this.clientFactory = checkNotNull(clientFactory);
        this.agentRegistry = checkNotNull(agentRegistry);
    }

    public Future<Agent> create(String agentName) {
        WaitAgentRegistration waitAgentRegistration = new WaitAgentRegistration(agentName);
        agentRegistry.addListener(waitAgentRegistration);
        return waitAgentRegistration.createAgent();
    }

    private final class WaitAgentRegistration implements AgentRegistryListener {
        private final String agentName;
        private final SettableFuture<Agent> future = SettableFuture.create();
        private Agent agent;

        WaitAgentRegistration(final String agentName) {
            this.agentName = agentName;
        }

        @Override
        public void onAgentRegistration(@NotNull final AgentRegistrationMessage msg) {
            if (msg.getName().equals(agent.getName())) {
                future.set(agent);
            }
        }

        @Override
        public void onAgentSignOff(@NotNull final String agent) {
            // Nothing to do here
        }

        Future<Agent> createAgent() {
            agent = new Agent(agentName, clientFactory, actorManagers);
            return future;
        }
    }
}