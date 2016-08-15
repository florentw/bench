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

import io.amaze.bench.client.runtime.agent.AgentInputMessage;
import io.amaze.bench.orchestrator.registry.ActorRegistryListener;
import io.amaze.bench.orchestrator.registry.AgentRegistryListener;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSServer;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.client.runtime.agent.Constants.AGENTS_ACTOR_NAME;
import static io.amaze.bench.client.runtime.agent.Constants.MASTER_ACTOR_NAME;

/**
 * Created on 3/8/16.
 */
final class JMSOrchestratorServer implements OrchestratorServer {

    private final JMSServer server;
    private final JMSClient client;

    JMSOrchestratorServer(@NotNull final JMSServer server, @NotNull final JMSClient client) {
        this.server = checkNotNull(server);
        this.client = checkNotNull(client);

        try {
            server.createQueue(MASTER_ACTOR_NAME);
            server.createTopic(AGENTS_ACTOR_NAME);

        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public void startRegistryListeners(@NotNull final AgentRegistryListener agentsListener,
                                       @NotNull final ActorRegistryListener actorsListener) {
        checkNotNull(agentsListener);
        checkNotNull(actorsListener);

        try {
            JMSMasterMessageListener msgListener = new JMSMasterMessageListener(agentsListener, actorsListener);
            client.addQueueListener(MASTER_ACTOR_NAME, msgListener);
            client.startListening();

        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public void createActorQueue(@NotNull final String actorName) {
        checkNotNull(actorName);

        try {
            server.createQueue(actorName);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public void deleteActorQueue(@NotNull final String actorName) {
        checkNotNull(actorName);

        server.deleteQueue(actorName);
    }

    @Override
    public void sendToActor(@NotNull final String actorName, @NotNull final Serializable message) {
        checkNotNull(actorName);
        checkNotNull(message);

        try {
            client.sendToQueue(actorName, message);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public void sendToAgent(@NotNull final AgentInputMessage message) {
        checkNotNull(message);

        try {
            client.sendToTopic(AGENTS_ACTOR_NAME, message);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public void close() {
        try {
            client.close();
        } finally {
            server.close();
        }
    }
}
