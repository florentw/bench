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
package io.amaze.bench.client.runtime.cluster.jms;

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.client.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.client.runtime.agent.AgentClientListener;
import io.amaze.bench.client.runtime.agent.AgentOutputMessage;
import io.amaze.bench.client.runtime.cluster.AgentClusterClient;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSException;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.client.runtime.agent.Constants.AGENTS_TOPIC;
import static io.amaze.bench.client.runtime.agent.Constants.REGISTRIES_TOPIC;

/**
 * Created on 4/24/16.
 */
final class JMSAgentClusterClient extends JMSClusterClient implements AgentClusterClient {

    private final String agent;

    @VisibleForTesting
    JMSAgentClusterClient(@NotNull final JMSClient client, @NotNull final String agent) {
        super(client);
        this.agent = checkNotNull(agent);
    }

    JMSAgentClusterClient(@NotNull final JMSEndpoint endpoint, @NotNull final String agent) {
        super(endpoint);
        this.agent = checkNotNull(agent);
    }

    @Override
    public void startAgentListener(@NotNull final String agentName, @NotNull final AgentClientListener listener) {
        checkNotNull(agentName);
        checkNotNull(listener);

        try {
            getClient().addTopicListener(AGENTS_TOPIC, new JMSAgentMessageListener(agentName, listener));
            getClient().startListening();
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public void sendToAgentRegistry(@NotNull final AgentOutputMessage message) {
        checkNotNull(message);

        try {
            getClient().sendToTopic(REGISTRIES_TOPIC, new Message<>(agent, message));
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public void sendToActorRegistry(@NotNull final ActorLifecycleMessage actorLifecycleMessage) {
        AgentOutputMessage agentRegistration = new AgentOutputMessage(AgentOutputMessage.Action.ACTOR_LIFECYCLE,
                                                                      actorLifecycleMessage);
        Message msg = new Message<>(agent, agentRegistration);
        sendToActorRegistry(msg);
    }

}
