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
 * Provides a listener on messages sent by other reactors.<br/>
 * The listener must be parametrized the type of the message class it accepts (parameter [I]).<br/>
 * <p/>
 * [I] the message type should be a pure data bean, and needs to be {@link Serializable},
 * it can be sent over the wire for inter-process communications.
 * <p/>
 * Created on 2/28/16.
 *
 * @param <I> Input message type
 * @see Sender
 * @see Before
 * @see After
 */
@FunctionalInterface
public interface Reactor<I extends Serializable> {

    /**
     * The message dispatch/handling logic of an actor is to be implemented here.<br/>
     * This method will be called for each received message from other reactors.
     *
     * @param from    Source reactor name, unique in the cluster
     * @param message Received payload, a data bean
     * @throws IrrecoverableException Non-recoverable exceptions, will provoke a close of the actor and a call
     *                                to the "@{@link After}" method if any. The exception will then be propagated
     *                                for troubleshooting purpose.
     * @throws TerminationException   An exception to be thrown by the Actor to signify that it needs to terminate
     *                                gracefully, it will provoke a close of the actor and a call
     *                                to the "@{@link After}" method if any.
     */
    void onMessage(@NotNull String from, @NotNull I message) throws ReactorException;

}
