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
package io.amaze.bench.api;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Interface to send arbitrary messages to another {@link Reactor}.<br/>
 * <br/>
 * Classes annotated with @{@link Actor} MUST NOT implement it.<br/>
 * A {@link Sender} can be passed as parameter of an actor's constructor, it will then be automatically injected by the runtime.
 * <ul>
 * <li>No check is performed on the destination address (The destination reactor may not exist).</li>
 * <li>No delivery guarantee, the message is sent synchronously.</li>
 * </ul>
 * Created on 2/28/16.
 *
 * @see Reactor
 */
@FunctionalInterface
public interface Sender {

    /**
     * Sends an arbitrary message to another {@link Reactor}.<br/>
     * No guarantee is made on the delivery. Sends the message asynchronously.
     *
     * @param to      Destination reactor's name, identifies uniquely a reactor in the cluster
     * @param message Payload to send
     */
    void send(@NotNull final String to, @NotNull final Serializable message);

}
