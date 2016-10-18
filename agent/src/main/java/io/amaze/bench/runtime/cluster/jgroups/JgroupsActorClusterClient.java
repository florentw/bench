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
package io.amaze.bench.runtime.cluster.jgroups;

import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.actor.ActorInputMessage;
import io.amaze.bench.runtime.actor.RuntimeActor;
import io.amaze.bench.runtime.actor.metric.MetricValuesMessage;
import io.amaze.bench.runtime.cluster.ActorClusterClient;
import io.amaze.bench.runtime.cluster.ActorRegistrySender;
import io.amaze.bench.runtime.cluster.ActorSender;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.shared.jgroups.JgroupsListener;
import io.amaze.bench.shared.jgroups.JgroupsListenerMultiplexer;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 9/30/16.
 */
public final class JgroupsActorClusterClient implements ActorClusterClient {

    private final Endpoint localEndpoint;
    private final JgroupsListenerMultiplexer multiplexer;
    private final JgroupsSender jgroupsSender;
    private final ActorRegistry actorRegistry;

    public JgroupsActorClusterClient(@NotNull final Endpoint localEndpoint,
                                     @NotNull final JgroupsListenerMultiplexer multiplexer,
                                     @NotNull final JgroupsSender jgroupsSender,
                                     @NotNull final ActorRegistry actorRegistry) {
        this.localEndpoint = checkNotNull(localEndpoint);
        this.multiplexer = checkNotNull(multiplexer);
        this.jgroupsSender = checkNotNull(jgroupsSender);
        this.actorRegistry = checkNotNull(actorRegistry);
    }

    @Override
    public void startActorListener(@NotNull final RuntimeActor actor) {
        checkNotNull(actor);

        multiplexer.addListener(ActorInputMessage.class, new MessageListener(actor));
    }

    @Override
    public void sendMetrics(@NotNull final MetricValuesMessage message) {
        checkNotNull(message);

        jgroupsSender.broadcast(message);
    }

    @Override
    public Endpoint getLocalEndpoint() {
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
        multiplexer.removeListenerFor(ActorInputMessage.class);
    }

    static final class MessageListener implements JgroupsListener<ActorInputMessage> {

        private final RuntimeActor actor;

        MessageListener(final RuntimeActor actor) {
            this.actor = actor;
        }

        @Override
        public void onMessage(@NotNull final org.jgroups.Message msg, @NotNull final ActorInputMessage input) {
            switch (input.getCommand()) {
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
                    actor.onMessage(input.getFrom(), input.getPayload());
                    break;
                default:
            }
        }
    }
}
