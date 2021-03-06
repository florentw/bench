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
package io.amaze.bench.cluster.jgroups;

import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.actor.ActorCreationRequest;
import io.amaze.bench.cluster.actor.ActorRegistrySender;
import io.amaze.bench.cluster.actor.ActorSender;
import io.amaze.bench.cluster.agent.*;
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.shared.jgroups.JgroupsListener;
import io.amaze.bench.shared.jgroups.JgroupsListenerMultiplexer;

import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Created on 10/2/16.
 */
public final class JgroupsAgentClusterClient implements AgentClusterClient {

    private final JgroupsListenerMultiplexer multiplexer;
    private final JgroupsSender jgroupsSender;
    private final ActorRegistry actorRegistry;
    private MessageListener jgroupsListener;

    JgroupsAgentClusterClient(@NotNull final JgroupsListenerMultiplexer multiplexer,
                              @NotNull final JgroupsSender jgroupsSender,
                              @NotNull final ActorRegistry actorRegistry) {

        this.multiplexer = requireNonNull(multiplexer);
        this.jgroupsSender = requireNonNull(jgroupsSender);
        this.actorRegistry = requireNonNull(actorRegistry);
    }

    @Override
    public void startAgentListener(@NotNull final AgentKey agent, @NotNull final AgentClientListener listener) {
        requireNonNull(agent);
        requireNonNull(listener);

        jgroupsListener = new MessageListener(agent, listener);
        multiplexer.addListener(AgentInputMessage.class, jgroupsListener);
    }

    @Override
    public AgentRegistrySender agentRegistrySender() {
        return new JgroupsAgentRegistrySender(jgroupsSender);
    }

    @Override
    public ActorRegistrySender actorRegistrySender() {
        return new JgroupsActorRegistrySender(jgroupsSender);
    }

    @Override
    public ActorSender actorSender() {
        return new JgroupsActorSender(jgroupsSender, actorRegistry);
    }

    @Override
    public void close() {
        multiplexer.removeListener(jgroupsListener);
    }

    static final class MessageListener implements JgroupsListener<AgentInputMessage> {
        private final AgentKey agent;
        private final AgentClientListener listener;

        MessageListener(final AgentKey agent, final AgentClientListener listener) {
            this.agent = agent;
            this.listener = listener;
        }

        @Override
        public void onMessage(@NotNull final org.jgroups.Message msg, @NotNull final AgentInputMessage inputMessage) {
            // Process messages only if sent to myself (topic)
            if (!inputMessage.getTargetAgent().equals(agent)) {
                return;
            }

            switch (inputMessage.getAction()) { // NOSONAR
                case CREATE_ACTOR:
                    createActor(listener, inputMessage);
                    break;
                case CLOSE_ACTOR:
                    closeActor(listener, inputMessage);
                    break;
                default:
            }
        }

        private void createActor(final AgentClientListener listener, final AgentInputMessage msg) {
            ActorCreationRequest data = msg.getCreationRequest();
            listener.onActorCreationRequest(data.getActorConfig());
        }

        private void closeActor(final AgentClientListener listener, final AgentInputMessage msg) {
            ActorKey actor = msg.getActorToClose();
            listener.onActorCloseRequest(actor);
        }
    }
}
