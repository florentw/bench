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

/**
 * A root type for errors to be thrown by an Actor while processing an incoming message.
 *
 * @see IrrecoverableException
 * @see TerminationException
 */
public abstract class ReactorException extends Exception {

    ReactorException() {
        // To be overridden
    }

    ReactorException(@NotNull final String message) {
        super(message);
    }

    ReactorException(@NotNull final String message, @NotNull final Throwable cause) {
        super(message, cause);
    }
}
