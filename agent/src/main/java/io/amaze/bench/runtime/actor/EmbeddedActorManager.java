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
package io.amaze.bench.runtime.actor;


import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.AgentClusterClientFactory;
import io.amaze.bench.cluster.actor.ActorConfig;
import io.amaze.bench.cluster.actor.RuntimeActor;
import io.amaze.bench.cluster.actor.ValidationException;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Instantiates actors using the given factory, in the local JVM.
 */
public class EmbeddedActorManager implements ActorManager {

    private final Actors actors;

    /**
     * @param clientFactory Will be used by the manager to give actors the ability to connect to the cluster.
     */
    public EmbeddedActorManager(@NotNull final AgentClusterClientFactory clientFactory) {
        super();
        actors = new Actors(clientFactory);
    }

    @NotNull
    @Override
    public ManagedActor createActor(@NotNull final ActorConfig actorConfig) throws ValidationException {
        checkNotNull(actorConfig);

        final ActorKey key = actorConfig.getKey();
        final RuntimeActor actor = actors.create(key, actorConfig.getClassName(), actorConfig.getActorJsonConfig());
        actor.init();

        return new ManagedActor() {
            @NotNull
            @Override
            public ActorKey getKey() {
                return key;
            }

            @Override
            public void close() {
                actor.close();
            }
        };
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
