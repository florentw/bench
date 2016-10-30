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

import io.amaze.bench.cluster.actor.ActorInputMessage;
import io.amaze.bench.cluster.actor.ActorKey;
import io.amaze.bench.cluster.actor.ActorSender;
import io.amaze.bench.cluster.leader.registry.ActorRegistry;
import io.amaze.bench.cluster.leader.registry.RegisteredActor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/18/16.
 */
public final class JgroupsActorSender implements ActorSender {

    private static final Logger log = LogManager.getLogger();

    private final JgroupsSender sender;
    private final ActorRegistry actorRegistry;

    public JgroupsActorSender(@NotNull final JgroupsSender sender, @NotNull final ActorRegistry actorRegistry) {
        this.sender = checkNotNull(sender);
        this.actorRegistry = checkNotNull(actorRegistry);
    }

    @Override
    public void send(@NotNull final ActorKey to, @NotNull final ActorInputMessage message) {
        checkNotNull(to);
        checkNotNull(message);
        log.debug("Sending {} to {}", message, to);

        // We need to resolve the endpoint using the actor's key first
        RegisteredActor registeredActor = actorRegistry.byKey(to);
        if (registeredActor == null || registeredActor.getDeployInfo() == null) {
            throw new NoSuchElementException("Cannot find endpoint for " + to + ".");
        }

        JgroupsActorMessage jgroupsMsg = new JgroupsActorMessage(to, message);
        sender.sendToEndpoint(registeredActor.getDeployInfo().getEndpoint(), jgroupsMsg);
    }
}
