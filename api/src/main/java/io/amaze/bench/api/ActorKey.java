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
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Identifies uniquely an actor instance across the cluster.
 * When implementing an actor you can have your own {@link ActorKey} injected in your public constructor.
 */
public final class ActorKey implements Key {

    private final String name;

    public ActorKey(@NotNull final String name) {
        this.name = requireNonNull(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActorKey actorKey = (ActorKey) o;
        return Objects.equals(name, actorKey.name);
    }

    @Override
    public String toString() {
        return "{\"actor\":\"" + name + "\"}";
    }
}
