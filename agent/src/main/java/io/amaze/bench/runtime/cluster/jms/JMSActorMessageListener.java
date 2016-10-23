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
package io.amaze.bench.runtime.cluster.jms;

import io.amaze.bench.api.Reactor;
import io.amaze.bench.runtime.actor.ActorInputMessage;
import io.amaze.bench.runtime.actor.RuntimeActor;
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
 * Convert received JMS messages for an actor to calls to its {@link Reactor} methods.
 */
final class JMSActorMessageListener implements MessageListener {

    private static final Logger log = LogManager.getLogger();

    private final RuntimeActor actor;

    JMSActorMessageListener(@NotNull final RuntimeActor actor) {
        this.actor = checkNotNull(actor);
    }

    @Override
    public void onMessage(@NotNull final Message jmsMessage) {
        checkNotNull(jmsMessage);

        Optional<ActorInputMessage> msg = readInputMessageFrom(jmsMessage);
        if (!msg.isPresent()) {
            return;
        }

        ActorInputMessage input = msg.get();
        switch (input.getCommand()) { // NOSONAR
            case CLOSE:
                actor.close();
                break;
            case DUMP_METRICS:
                actor.dumpAndFlushMetrics();
                break;
            case MESSAGE:
                actor.onMessage(input.getFrom(), input.getPayload());
                break;
            default:
        }
    }

    private Optional<ActorInputMessage> readInputMessageFrom(@NotNull final Message jmsMessage) {
        try {
            return Optional.of(JMSHelper.objectFromMsg((BytesMessage) jmsMessage));
        } catch (Exception e) {
            log.error("Invalid ActorInputMessage received, jmsMessage:{}", jmsMessage, e);
            return Optional.empty();
        }
    }
}
