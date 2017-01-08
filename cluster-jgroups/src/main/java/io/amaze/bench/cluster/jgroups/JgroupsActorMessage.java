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

import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.actor.ActorInputMessage;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class to be used to send a message to an actor on Jgroups implementation.<br/>
 * It is needed to now to which actor to deliver the message when they share the same JChannel (embedded mode).
 *
 * @see ActorInputMessage Payload for actors
 */
public final class JgroupsActorMessage implements Serializable {

    private final ActorKey to;
    private final ActorInputMessage inputMessage;

    public JgroupsActorMessage(final ActorKey to, final ActorInputMessage inputMessage) {
        this.to = checkNotNull(to);
        this.inputMessage = checkNotNull(inputMessage);
    }

    public ActorKey to() {
        return to;
    }

    public ActorInputMessage inputMessage() {
        return inputMessage;
    }

    @Override
    public String toString() {
        return "{\"JgroupsActorMessage\":{" +  //
                "\"to\":" + to + ", " + //
                "\"inputMessage\":" + inputMessage + "}}";
    }
}
