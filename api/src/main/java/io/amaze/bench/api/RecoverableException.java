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
 * To be thrown by an Actor when a recoverable error happens. A recoverable error means,<br>
 * that the actor can still process new messages seamlessly.<br>
 *
 * It can notify the agent of the failure, that can perform additional actions to help troubleshoot the issue.
 *
 * @see IrrecoverableException An exception that can be thrown to notify the agent of a fatal error.
 */
public final class RecoverableException extends ReactorException {

    public RecoverableException(@NotNull final String message) {
        super(checkNotNull(message));
    }

    public RecoverableException(@NotNull final String message, @NotNull final Throwable cause) {
        super(checkNotNull(message), checkNotNull(cause));
    }
}

