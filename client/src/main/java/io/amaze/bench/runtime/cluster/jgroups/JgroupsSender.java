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

import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.RegisteredActor;
import org.jgroups.JChannel;
import org.jgroups.util.Util;

import java.io.Serializable;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

/**
 * Created on 10/1/16.
 */
public final class JgroupsSender {

    private final JChannel channel;
    private final ActorRegistry actorRegistry;

    public JgroupsSender(final JChannel channel, final ActorRegistry actorRegistry) {
        this.channel = checkNotNull(channel);
        this.actorRegistry = checkNotNull(actorRegistry);
    }

    public void broadcast(Serializable message) {
        try {
            channel.send(null, Util.objectToByteBuffer(message));
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    public void sendToActor(ActorKey actor, Serializable message) {
        RegisteredActor registeredActor = actorRegistry.byKey(actor);
        if (registeredActor == null) {
            throw new NoSuchElementException("Cannot find " + actor + ".");
        }

        try {
            channel.send(null, Util.objectToByteBuffer(message));
        } catch (Exception e) {
            throw propagate(e);
        }
    }

}
