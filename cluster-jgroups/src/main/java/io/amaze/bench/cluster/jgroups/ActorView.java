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
package io.amaze.bench.cluster.jgroups;

import io.amaze.bench.cluster.registry.RegisteredActor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/2/16.
 */
public final class ActorView implements Serializable {

    private final Set<RegisteredActor> registeredActors; // NOSONAR: It is serializable

    public ActorView(final Set<RegisteredActor> registeredActors) {
        this.registeredActors = checkNotNull(registeredActors);
    }

    public Set<RegisteredActor> registeredActors() {
        return new HashSet<>(registeredActors);
    }
}
