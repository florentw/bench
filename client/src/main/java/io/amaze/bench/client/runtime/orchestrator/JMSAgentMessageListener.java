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

import io.amaze.bench.client.api.Reactor;
import io.amaze.bench.client.runtime.agent.AgentClientListener;
import io.amaze.bench.client.runtime.agent.AgentInputMessage;
import io.amaze.bench.shared.jms.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Convert received JMS messages from the master to the agent to calls to its {@link Reactor} methods.
 */
final class JMSAgentMessageListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(JMSAgentMessageListener.class);

    private final AgentClientListener listener;
    private final String agentName;

    JMSAgentMessageListener(@NotNull final String agentName, @NotNull final AgentClientListener listener) {
        this.agentName = checkNotNull(agentName);
        this.listener = checkNotNull(listener);
    }

    @Override
    public void onMessage(@NotNull final javax.jms.Message message) {
        checkNotNull(message);

        AgentInputMessage msg = readInputMessage(message);
        if (msg == null) {
            return;
        }

        // Process messages only if sent to myself (topic)
        if (!msg.getDestinationAgent().equals(agentName)) {
            return;
        }

        switch (msg.getAction()) { // NOSONAR
            case CREATE_ACTOR:
                createActor(msg);
                break;
            case CLOSE_ACTOR:
                closeActor(msg);
                break;
        }
    }

    private AgentInputMessage readInputMessage(@NotNull final Message message) {
        try {
            return JMSHelper.objectFromMsg((BytesMessage) message);
        } catch (Exception e) {
            LOG.error("Invalid AgentInputMessage received, message:" + message, e);
            return null;
        }
    }

    private void closeActor(final AgentInputMessage msg) {
        String aToClose = (String) msg.getData();
        listener.onActorCloseRequest(aToClose);
    }

    private void createActor(final AgentInputMessage msg) {
        ActorCreationRequest data = (ActorCreationRequest) msg.getData();
        listener.onActorCreationRequest(data.getActorConfig());
    }
}
