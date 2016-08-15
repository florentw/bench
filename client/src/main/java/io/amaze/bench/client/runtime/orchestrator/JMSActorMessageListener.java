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
import io.amaze.bench.client.runtime.actor.Actor;
import io.amaze.bench.client.runtime.actor.ActorInputMessage;
import io.amaze.bench.shared.jms.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Convert received JMS messages for an actor to calls to its {@link Reactor} methods.
 */
final class JMSActorMessageListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(JMSActorMessageListener.class);

    private final Actor actor;

    JMSActorMessageListener(@NotNull final Actor actor) {
        this.actor = checkNotNull(actor);
    }

    @Override
    public void onMessage(@NotNull final javax.jms.Message message) {
        checkNotNull(message);

        ActorInputMessage msg = readInputMessage(message);
        if (msg == null) {
            return;
        }

        switch (msg.getCommand()) { // NOSONAR
            case INIT:
                actor.init();
                break;
            case CLOSE:
                actor.close();
                break;
            case DUMP_METRICS:
                actor.dumpAndFlushMetrics();
                break;
            case MESSAGE:
                actor.onMessage(msg.getFrom(), msg.getPayload());
                break;
        }
    }

    private ActorInputMessage readInputMessage(@NotNull final Message message) {
        try {
            return JMSHelper.objectFromMsg((BytesMessage) message);
        } catch (Exception e) {
            LOG.error("Invalid ActorInputMessage received, message:" + message, e);
            return null;
        }
    }
}
