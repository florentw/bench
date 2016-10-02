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
import io.amaze.bench.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.runtime.agent.AgentClientListener;
import io.amaze.bench.runtime.agent.AgentInputMessage;
import io.amaze.bench.runtime.agent.AgentLifecycleMessage;
import io.amaze.bench.runtime.cluster.ActorCreationRequest;
import io.amaze.bench.runtime.cluster.AgentClusterClient;
import io.amaze.bench.runtime.message.Message;
import io.amaze.bench.shared.jgroups.JgroupsListenerMultiplexer;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/2/16.
 */
public final class JgroupsAgentClusterClient implements AgentClusterClient {

    private final JgroupsListenerMultiplexer multiplexer;
    private final JgroupsSender sender;

    JgroupsAgentClusterClient(@NotNull final JgroupsListenerMultiplexer multiplexer,
                              @NotNull final JgroupsSender sender) {
        this.multiplexer = checkNotNull(multiplexer);
        this.sender = checkNotNull(sender);
    }

    @Override
    public void startAgentListener(@NotNull final String agent, @NotNull final AgentClientListener listener) {
        checkNotNull(agent);
        checkNotNull(listener);

        multiplexer.addListener(AgentInputMessage.class, (msg, inputMessage) -> {

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
        });
    }

    @Override
    public void sendToAgentRegistry(@NotNull final AgentLifecycleMessage message) {
        sender.broadcast(message);
    }

    @Override
    public void sendToActorRegistry(@NotNull final ActorLifecycleMessage actorLifecycleMessage) {
        sender.broadcast(actorLifecycleMessage);
    }

    @Override
    public void sendToActor(@NotNull final String to, @NotNull final Message<? extends Serializable> message) {
        sender.sendToActor(new ActorKey(to), message);
    }

    @Override
    public void close() {
        multiplexer.removeListenerFor(AgentInputMessage.class);
    }

    private void closeActor(final AgentClientListener listener, final AgentInputMessage msg) {
        ActorKey actor = msg.getActorToClose();
        listener.onActorCloseRequest(actor);
    }

    private void createActor(final AgentClientListener listener, final AgentInputMessage msg) {
        ActorCreationRequest data = msg.getCreationRequest();
        listener.onActorCreationRequest(data.getActorConfig());
    }
}
