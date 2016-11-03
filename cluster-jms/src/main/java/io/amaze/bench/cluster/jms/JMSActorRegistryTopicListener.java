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

import io.amaze.bench.cluster.Message;
import io.amaze.bench.cluster.actor.ActorLifecycleMessage;
import io.amaze.bench.cluster.registry.ActorRegistryListener;
import io.amaze.bench.shared.jms.JMSHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.BytesMessage;
import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 9/25/16.
 */
public final class JMSActorRegistryTopicListener implements MessageListener {

    private static final Logger log = LogManager.getLogger();

    private final ActorRegistryListener registryListener;

    JMSActorRegistryTopicListener(@NotNull final ActorRegistryListener registryListener) {
        this.registryListener = checkNotNull(registryListener);
    }

    @Override
    public void onMessage(final javax.jms.Message message) {
        javax.jms.Message jmsMessage = checkNotNull(message);

        Optional<Message> received = readMessage(jmsMessage);
        if (!received.isPresent()) {
            return;
        }

        try {
            ActorLifecycleMessage lifecycleMessage = (ActorLifecycleMessage) received.get().data();

            lifecycleMessage.sendTo(registryListener);

        } catch (Exception e) { // NOSONAR - We want to catch everything
            log.error("Error handling actor registry message {}", received.get(), e);
        }
    }

    private Optional<Message> readMessage(final javax.jms.Message jmsMessage) {
        try {
            return Optional.of(JMSHelper.objectFromMsg((BytesMessage) jmsMessage));
        } catch (Exception e) { // NOSONAR - We want to catch everything
            log.error("Error while reading JMS message.", e);
            return Optional.empty();
        }
    }
}
