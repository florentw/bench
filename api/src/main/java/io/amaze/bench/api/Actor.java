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

import java.lang.annotation.*;

/**
 * Annotation to be used on actor classes.<br>
 * <ul>
 * <li>An class annotated with @{@link Actor} must implement the {@link Reactor} interface.<br></li>
 * <li>The {@link Reactor} interface must be parametrized with the type of messages it will take as input.<br></li>
 * <li>An actor class must have a public constructor that will be used to inject its dependencies.</li>
 * <li>An actor can declare one of its method as a setup routine, by it annotating with @{@link Before}</li>
 * <li>An actor can declare one of its method as a teardown routine, by it annotating with @{@link After}</li>
 * </ul>
 *
 * @see Reactor To be implemented by classes annotated with @{@link Actor}.
 * @see Sender Can be passed in the public constructor of a class annotated with @{@link Actor}.
 */
@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Actor {

}
