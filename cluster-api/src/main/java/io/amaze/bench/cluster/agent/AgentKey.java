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
package io.amaze.bench.cluster.agent;

import io.amaze.bench.api.Key;

import javax.validation.constraints.NotNull;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Identifies uniquely an agent instance across the cluster.
 */
public final class AgentKey implements Key {

    private final String name;

    public AgentKey(@NotNull final String name) {
        this.name = checkNotNull(name);
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
        AgentKey agentKey = (AgentKey) o;
        return Objects.equals(name, agentKey.name);
    }

    @Override
    public String toString() {
        return "{\"agent\":\"" + name + "\"}";
    }


}
