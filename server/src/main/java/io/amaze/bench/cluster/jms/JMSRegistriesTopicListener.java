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

import io.amaze.bench.client.runtime.LifecycleMessage;
import io.amaze.bench.client.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.client.runtime.agent.AgentLifecycleMessage;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.cluster.registry.ActorRegistryListener;
import io.amaze.bench.cluster.registry.AgentRegistryListener;
import io.amaze.bench.shared.jms.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.BytesMessage;
import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/28/16.
 */
final class JMSRegistriesTopicListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(JMSRegistriesTopicListener.class);

    private final AgentRegistryListener agentsListener;
    private final ActorRegistryListener actorsListener;

    JMSRegistriesTopicListener(@NotNull final AgentRegistryListener agentsListener,
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

        try {
            onRegistryMessage(received.get());
        } catch (Exception e) {
            LOG.error("Error handling registry message " + received.get(), e);
        }
    }

    private Optional<Message> readMessage(final javax.jms.Message jmsMessage) {
        try {
            return Optional.of(JMSHelper.objectFromMsg((BytesMessage) jmsMessage));
        } catch (Exception e) {
            LOG.error("Error while reading JMS message.", e);
            return Optional.empty();
        }
    }

    private void onRegistryMessage(final Message received) {
        LifecycleMessage lifecycleMessage = (LifecycleMessage) received.data();
        if (lifecycleMessage instanceof AgentLifecycleMessage) {
            AgentLifecycleMessage agentMsg = (AgentLifecycleMessage) lifecycleMessage;
            onAgentLifecycle(agentMsg);
        } else {
            ActorLifecycleMessage actorLifecycleMessage = (ActorLifecycleMessage) lifecycleMessage;
            onActorLifecycle(actorLifecycleMessage);
        }
    }

    private void onAgentLifecycle(final AgentLifecycleMessage agentMsg) {
        switch (agentMsg.getState()) {
            case CREATED:
                onAgentRegistration(agentMsg);
                break;
            case CLOSED:
                onAgentSignOff(agentMsg);
                break;
            default:
                break;
        }
    }

    private void onActorLifecycle(final ActorLifecycleMessage lfMsg) {
        String actor = lfMsg.getActor();

        switch (lfMsg.getState()) {
            case CREATED:
                actorsListener.onActorCreated(actor, lfMsg.getAgent());
                break;
            case INITIALIZED:
                actorsListener.onActorInitialized(actor, lfMsg.getDeployInfo());
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

    private void onAgentSignOff(final AgentLifecycleMessage received) {
        String agentName = received.getAgent();
        agentsListener.onAgentSignOff(agentName);
    }

    private void onAgentRegistration(final AgentLifecycleMessage received) {
        agentsListener.onAgentRegistration(received.getRegistrationMessage());
    }
}
