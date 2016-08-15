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
package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.api.Reactor;

import javax.validation.constraints.NotNull;

/**
 * Provides a {@link ActorValidator} instance to use for loading {@link Reactor} classes.
 * <p>
 * Created on 3/2/16.
 *
 * @see ActorValidator
 */
final class ActorValidators {

    private static final ActorValidator impl = new ActorValidatorImpl();

    private ActorValidators() {
        // Factory
    }

    /**
     * @return An Actor validator instance.
     */
    @NotNull
    public static ActorValidator get() {
        return impl;
    }

}
