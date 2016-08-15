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
package io.amaze.bench.client.api;

import javax.validation.constraints.NotNull;

/**
 * To be thrown by an Actor when a non-recoverable error happens.<br/>
 * It will notify the agent of the failure, that can perform additional actions to help troubleshoot the issue.
 */
public final class IrrecoverableException extends ReactorException {

    public IrrecoverableException(@NotNull final String message) {
        super(message);
    }

    public IrrecoverableException(@NotNull final String message, @NotNull final Throwable cause) {
        super(message, cause);
    }
}
