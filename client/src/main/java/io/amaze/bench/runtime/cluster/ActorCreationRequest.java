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
package io.amaze.bench.runtime.cluster;

import io.amaze.bench.runtime.actor.ActorConfig;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Message sent by the resource manager to an agent when it is required to create an actor.
 */
public final class ActorCreationRequest implements Serializable {

    private final ActorConfig actorConfig;

    public ActorCreationRequest(@NotNull final ActorConfig actorConfig) {
        this.actorConfig = checkNotNull(actorConfig);
    }

    public ActorConfig getActorConfig() {
        return actorConfig;
    }

    @Override
    public int hashCode() {
        return Objects.hash(actorConfig);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActorCreationRequest that = (ActorCreationRequest) o;
        return Objects.equals(actorConfig, that.actorConfig);
    }
}
