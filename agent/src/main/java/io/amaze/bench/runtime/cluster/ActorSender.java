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
package io.amaze.bench.runtime.cluster;

import io.amaze.bench.runtime.cluster.actor.ActorInputMessage;
import io.amaze.bench.runtime.cluster.actor.ActorKey;

import javax.validation.constraints.NotNull;

/**
 * Created on 10/16/16.
 */
@FunctionalInterface
public interface ActorSender {

    /**
     * A call to this method will send a message {@code message} to the target actor {@code to}
     * using the underlying messaging system.
     *
     * @param to      Target actor key
     * @param message Payload to send
     */
    void send(@NotNull final ActorKey to, @NotNull final ActorInputMessage message);

}
