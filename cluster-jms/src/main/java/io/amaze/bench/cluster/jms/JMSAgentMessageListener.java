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
package io.amaze.bench.cluster.jms;

import io.amaze.bench.api.ActorKey;
import io.amaze.bench.api.Reactor;
import io.amaze.bench.cluster.actor.ActorCreationRequest;
import io.amaze.bench.cluster.agent.AgentClientListener;
import io.amaze.bench.cluster.agent.AgentInputMessage;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.shared.jms.JMSHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Convert received JMS messages from the resource manager to the agent to calls to its {@link Reactor} methods.
 */
final class JMSAgentMessageListener implements MessageListener {

    private static final Logger log = LogManager.getLogger();

    private final AgentClientListener listener;
    private final AgentKey agentKey;

    JMSAgentMessageListener(@NotNull final AgentKey agentKey, @NotNull final AgentClientListener listener) {
        this.agentKey = checkNotNull(agentKey);
        this.listener = checkNotNull(listener);
    }

    @Override
    public void onMessage(@NotNull final Message jmsMessage) {
        checkNotNull(jmsMessage);

        Optional<AgentInputMessage> msg = readInputMessageFrom(jmsMessage);
        if (!msg.isPresent()) {
            return;
        }

        AgentInputMessage inputMessage = msg.get();

        // Process messages only if sent to myself (topic)
        if (!inputMessage.getTargetAgent().equals(agentKey)) {
            return;
        }

        switch (inputMessage.getAction()) { // NOSONAR
            case CREATE_ACTOR:
                createActor(inputMessage);
                break;
            case CLOSE_ACTOR:
                closeActor(inputMessage);
                break;
            default:
        }
    }

    private static Optional<AgentInputMessage> readInputMessageFrom(@NotNull final Message jmsMessage) {
        try {
            return Optional.of(JMSHelper.objectFromMsg((BytesMessage) jmsMessage));
        } catch (Exception e) { // NOSONAR - We want to catch everything
            log.error("Invalid AgentInputMessage received, jmsMessage:{}", jmsMessage, e);
            return Optional.empty();
        }
    }

    private void closeActor(final AgentInputMessage msg) {
        ActorKey actor = msg.getActorToClose();
        listener.onActorCloseRequest(actor);
    }

    private void createActor(final AgentInputMessage msg) {
        ActorCreationRequest data = msg.getCreationRequest();
        listener.onActorCreationRequest(data.getActorConfig());
    }
}
