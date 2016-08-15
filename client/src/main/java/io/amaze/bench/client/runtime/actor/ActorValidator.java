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
 * In charge of loading the {@link Reactor} classes and validating them, so that they can be handled by the runtime.
 * <p>
 * Created on 3/1/16.
 *
 * @see ActorValidators
 * @see Reactor
 */
interface ActorValidator {

    /**
     * Attempts to load the {@link Reactor} class using the current classpath,<br/>
     * a set of checks is then performed on the resulting Class,<br/>
     * if one or more checks fail, a {@link ValidationException} is thrown.
     *
     * @param className Fully qualified name of the class implementing {@link Reactor}.
     * @return The loaded class.
     * @throws ValidationException If routine checks failed after loading the class.
     */
    @NotNull
    Class<? extends Reactor> loadAndValidate(@NotNull String className) throws ValidationException;

}
