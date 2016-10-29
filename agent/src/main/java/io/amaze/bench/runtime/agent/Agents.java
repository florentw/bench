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

import com.google.common.util.concurrent.SettableFuture;
import io.amaze.bench.cluster.ClusterClientFactory;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.agent.AgentRegistrationMessage;
import io.amaze.bench.cluster.registry.AgentRegistry;
import io.amaze.bench.cluster.registry.AgentRegistryListener;
import io.amaze.bench.runtime.actor.ActorManagers;

import javax.validation.constraints.NotNull;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;

/**
 * Created on 9/12/16.
 */
public final class Agents {

    private final ActorManagers actorManagers;
    private final ClusterClientFactory clientFactory;
    private final AgentRegistry agentRegistry;

    public Agents(@NotNull final ActorManagers actorManagers,
                  @NotNull final ClusterClientFactory clientFactory,
                  @NotNull final AgentRegistry agentRegistry) {

        this.actorManagers = checkNotNull(actorManagers);
        this.clientFactory = checkNotNull(clientFactory);
        this.agentRegistry = checkNotNull(agentRegistry);
    }

    /**
     * Triggers an {@link Agent} instance creation. Returns a future that is set when it successfully registered.
     *
     * @param agentKey Key of the agent to be created.
     * @return A future that will be set with the Agent instance is successful registration.
     * If a sign off message is received, a {@link AgentSignOffException} is set.
     */
    public Future<Agent> create(@NotNull final AgentKey agentKey) {
        checkNotNull(agentKey);
        WaitAgentRegistration waitAgentRegistration = new WaitAgentRegistration(agentKey);
        agentRegistry.addListener(waitAgentRegistration);
        return waitAgentRegistration.createAgent();
    }

    /**
     * Is set when the agent signs off.
     */
    public static final class AgentSignOffException extends Exception {

    }

    /**
     * Is set when the agent fails.
     */
    public static final class AgentFailureException extends Exception {

    }

    private final class WaitAgentRegistration implements AgentRegistryListener {
        private final AgentKey agentKey;
        private final SettableFuture<Agent> future = SettableFuture.create();
        private final CountDownLatch agentCreated = new CountDownLatch(1);
        private Agent agent;

        WaitAgentRegistration(final AgentKey agentKey) {
            this.agentKey = agentKey;
        }

        @Override
        public void onAgentRegistration(@NotNull final AgentRegistrationMessage msg) {
            checkNotNull(msg);

            awaitUninterruptibly(agentCreated);

            if (msg.getKey().equals(agent.getKey())) {
                future.set(agent);
                agentRegistry.removeListener(this);
            }
        }

        @Override
        public void onAgentSignOff(@NotNull final AgentKey agentKey) {
            checkNotNull(agentKey);

            awaitUninterruptibly(agentCreated);

            if (agentKey.equals(agent.getKey())) {
                future.setException(new AgentSignOffException());
                agentRegistry.removeListener(this);
            }
        }

        @Override
        public void onAgentFailed(@NotNull final AgentKey agent, @NotNull final Throwable throwable) {
            checkNotNull(agentKey);
            checkNotNull(throwable);

            awaitUninterruptibly(agentCreated);

            if (agentKey.equals(agent)) {
                future.setException(new AgentFailureException());
                agentRegistry.removeListener(this);
            }
        }

        Future<Agent> createAgent() {
            agent = new Agent(agentKey, clientFactory, actorManagers);
            agentCreated.countDown();
            return future;
        }
    }
}
