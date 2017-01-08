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
package io.amaze.bench.api;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * To be thrown by an Actor when a non-recoverable error happens.<br>
 * It means that the actor cannot process further messages and needs to be terminated.<br>
 *
 * Note that when thrown, the agent will attempt to call its {@link After} method if any before destroying the actor.
 *
 * The agent can then perform additional actions to help troubleshoot the issue (log the error, etc.)
 *
 * @see RecoverableException An exception that can be recovered from by the actor
 */
public final class IrrecoverableException extends ReactorException {

    public IrrecoverableException(@NotNull final String message) {
        super(checkNotNull(message));
    }

    public IrrecoverableException(@NotNull final String message, @NotNull final Throwable cause) {
        super(checkNotNull(message), checkNotNull(cause));
    }
}
