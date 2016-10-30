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
package io.amaze.bench.runtime.cluster.jgroups;

import io.amaze.bench.cluster.registry.RegisteredAgent;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/14/16.
 */
public final class AgentView implements Serializable {

    private final Set<RegisteredAgent> registeredAgents; // NOSONAR: It is serializable

    public AgentView(final Set<RegisteredAgent> registeredAgents) {
        this.registeredAgents = checkNotNull(registeredAgents);
    }

    public Set<RegisteredAgent> registeredAgents() {
        return new HashSet<>(registeredAgents);
    }
}
