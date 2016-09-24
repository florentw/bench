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
import io.amaze.bench.client.runtime.actor.RuntimeActor;
import io.amaze.bench.client.runtime.cluster.ActorClusterClient;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSException;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

/**
 * Created on 4/24/16.
 */
final class JMSActorClusterClient extends JMSClusterClient implements ActorClusterClient {

    private final String actor;

    @VisibleForTesting
    JMSActorClusterClient(@NotNull final JMSClient client, @NotNull final String actor) {
        super(client);
        this.actor = checkNotNull(actor);
    }

    JMSActorClusterClient(@NotNull final JMSEndpoint endpoint, @NotNull final String actor) {
        super(endpoint);
        this.actor = checkNotNull(actor);
    }

    @Override
    public void sendToActorRegistry(@NotNull final ActorLifecycleMessage actorLifecycleMessage) {
        Message msg = new Message<>(actor, actorLifecycleMessage);
        sendToActorRegistry(msg);
    }

    @Override
    public void startActorListener(@NotNull final RuntimeActor actor) {
        checkNotNull(actor);

        try {
            getClient().addQueueListener(actor.name(), new JMSActorMessageListener(actor));
            getClient().startListening();
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

}
