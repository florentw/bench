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
package io.amaze.bench.runtime.cluster.jgroups;

import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.agent.AgentClientListener;
import io.amaze.bench.runtime.agent.AgentInputMessage;
import io.amaze.bench.runtime.cluster.*;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.shared.jgroups.JgroupsListener;
import io.amaze.bench.shared.jgroups.JgroupsListenerMultiplexer;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

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

        this.multiplexer = checkNotNull(multiplexer);
        this.jgroupsSender = checkNotNull(jgroupsSender);
        this.actorRegistry = checkNotNull(actorRegistry);
    }

    @Override
    public void startAgentListener(@NotNull final String agent, @NotNull final AgentClientListener listener) {
        checkNotNull(agent);
        checkNotNull(listener);

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
        private final String agent;
        private final AgentClientListener listener;

        MessageListener(final String agent, final AgentClientListener listener) {
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
