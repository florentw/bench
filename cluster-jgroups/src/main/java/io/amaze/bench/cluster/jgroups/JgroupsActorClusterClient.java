/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
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
package io.amaze.bench.cluster.jgroups;

import io.amaze.bench.cluster.Endpoint;
import io.amaze.bench.cluster.actor.*;
import io.amaze.bench.cluster.metric.MetricValuesMessage;
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.shared.jgroups.JgroupsListener;
import io.amaze.bench.shared.jgroups.JgroupsListenerMultiplexer;

import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Created on 9/30/16.
 */
public final class JgroupsActorClusterClient implements ActorClusterClient {

    private final Endpoint localEndpoint;
    private final JgroupsListenerMultiplexer multiplexer;
    private final JgroupsSender jgroupsSender;
    private final ActorRegistry actorRegistry;
    private MessageListener listener;

    JgroupsActorClusterClient(@NotNull final Endpoint localEndpoint,
                              @NotNull final JgroupsListenerMultiplexer multiplexer,
                              @NotNull final JgroupsSender jgroupsSender,
                              @NotNull final ActorRegistry actorRegistry) {

        this.localEndpoint = requireNonNull(localEndpoint);
        this.multiplexer = requireNonNull(multiplexer);
        this.jgroupsSender = requireNonNull(jgroupsSender);
        this.actorRegistry = requireNonNull(actorRegistry);
    }

    @Override
    public void startActorListener(@NotNull final RuntimeActor actor) {
        requireNonNull(actor);

        listener = new MessageListener(actor);
        multiplexer.addListener(JgroupsActorMessage.class, listener);
    }

    @Override
    public void sendMetrics(@NotNull final MetricValuesMessage message) {
        requireNonNull(message);

        jgroupsSender.broadcast(message);
    }

    @Override
    public Endpoint localEndpoint() {
        return localEndpoint;
    }

    @Override
    public ActorSender actorSender() {
        return new JgroupsActorSender(jgroupsSender, actorRegistry);
    }

    @Override
    public ActorRegistrySender actorRegistrySender() {
        return new JgroupsActorRegistrySender(jgroupsSender);
    }

    @Override
    public void close() {
        multiplexer.removeListener(listener);
    }

    static final class MessageListener implements JgroupsListener<JgroupsActorMessage> {

        private final RuntimeActor actor;

        MessageListener(final RuntimeActor actor) {
            this.actor = actor;
        }

        @Override
        public void onMessage(@NotNull final org.jgroups.Message msg, @NotNull final JgroupsActorMessage jgroupsMsg) {
            if (!jgroupsMsg.to().equals(actor.getKey())) {
                return;
            }

            ActorInputMessage input = jgroupsMsg.inputMessage();
            switch (input.getCommand()) {
                case BOOTSTRAP:
                    actor.bootstrap();
                    break;
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
                    throw new UnsupportedOperationException();
            }
        }
    }
}
