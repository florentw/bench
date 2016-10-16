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

import io.amaze.bench.runtime.actor.ActorInputMessage;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.cluster.ActorSender;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.RegisteredActor;
import io.amaze.bench.shared.jgroups.JgroupsEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.util.Util;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

/**
 * Created on 10/1/16.
 */
public class JgroupsSender implements ActorSender {

    private static final Logger log = LogManager.getLogger();

    private final JChannel channel;
    private final ActorRegistry actorRegistry;

    public JgroupsSender(@NotNull final JChannel channel, @NotNull final ActorRegistry actorRegistry) {
        this.channel = checkNotNull(channel);
        this.actorRegistry = checkNotNull(actorRegistry);
    }

    public void broadcast(@NotNull final Serializable message) {
        checkNotNull(message);
        log.debug("Broadcasting {} on {}", message, channel);

        try {
            channel.send(null, Util.objectToByteBuffer(message));
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    @Override
    public void sendToActor(@NotNull final ActorKey to, @NotNull final ActorInputMessage message) {
        checkNotNull(to);
        checkNotNull(message);
        log.debug("Sending {} to actor {} on {}", message, to, channel);

        // We need to resolve the endpoint using the actor's name first
        RegisteredActor registeredActor = actorRegistry.byKey(to);
        if (registeredActor == null || registeredActor.getDeployInfo() == null) {
            throw new NoSuchElementException("Cannot find endpoint for " + to + ".");
        }

        try {
            JgroupsEndpoint endpoint = registeredActor.getDeployInfo().getEndpoint();
            channel.send(endpoint.getAddress(), Util.objectToByteBuffer(message));
        } catch (Exception e) {
            throw propagate(e);
        }
    }

}
