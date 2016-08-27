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

import com.google.common.base.Optional;
import io.amaze.bench.client.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.client.runtime.agent.AgentOutputMessage;
import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.orchestrator.registry.ActorRegistryListener;
import io.amaze.bench.orchestrator.registry.AgentRegistryListener;
import io.amaze.bench.shared.jms.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.BytesMessage;
import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/28/16.
 */
final class JMSMasterMessageListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(JMSMasterMessageListener.class);

    private final AgentRegistryListener agentsListener;
    private final ActorRegistryListener actorsListener;

    JMSMasterMessageListener(@NotNull final AgentRegistryListener agentsListener,
                             @NotNull final ActorRegistryListener actorsListener) {
        this.agentsListener = checkNotNull(agentsListener);
        this.actorsListener = checkNotNull(actorsListener);
    }

    @Override
    public void onMessage(final javax.jms.Message message) {
        javax.jms.Message jmsMessage = checkNotNull(message);

        Optional<Message> received = readMessage(jmsMessage);
        if (!received.isPresent()) {
            return;
        }

        Message internalMessage = received.get();
        if (internalMessage.data() instanceof AgentOutputMessage) {
            onMasterMessage(internalMessage);
        } else {
            LOG.error("Received invalid message \"" + received + "\" (not a AgentOutputMessage).");
        }
    }

    private Optional<Message> readMessage(final javax.jms.Message jmsMessage) {
        try {
            return Optional.of((Message) JMSHelper.objectFromMsg((BytesMessage) jmsMessage));
        } catch (Exception e) {
            LOG.error("Error while reading JMS message  as AgentOutputMessage.", e);
            return Optional.absent();
        }
    }

    private void onMasterMessage(final io.amaze.bench.client.runtime.message.Message received) {
        AgentOutputMessage masterMsg = (AgentOutputMessage) received.data();
        switch (masterMsg.getAction()) { // NOSONAR
            case REGISTER_AGENT:
                onAgentRegistration(masterMsg);
                break;
            case UNREGISTER_AGENT:
                onAgentSignOff(masterMsg);
                break;
            case ACTOR_LIFECYCLE:
                onActorLifecycle(masterMsg);
                break;
            default:
        }
    }

    private void onActorLifecycle(final AgentOutputMessage received) {
        ActorLifecycleMessage lfMsg = (ActorLifecycleMessage) received.getData();
        String actor = lfMsg.getActor();

        switch (lfMsg.getPhase()) { // NOSONAR
            case CREATED:
                actorsListener.onActorCreated(actor, lfMsg.getAgent());
                break;
            case INITIALIZED:
                actorsListener.onActorInitialized(actor, lfMsg.getAgent());
                break;
            case FAILED:
                actorsListener.onActorFailed(actor, lfMsg.getThrowable());
                break;
            case CLOSED:
                actorsListener.onActorClosed(actor);
                break;
            default:
        }
    }

    private void onAgentSignOff(final AgentOutputMessage received) {
        String actorName = (String) received.getData();
        agentsListener.onAgentSignOff(actorName);
    }

    private void onAgentRegistration(final AgentOutputMessage received) {
        AgentRegistrationMessage regMsg = (AgentRegistrationMessage) received.getData();
        agentsListener.onAgentRegistration(regMsg);
    }
}
