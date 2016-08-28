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


import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Instantiates actors using the given factory, in the local JVM.
 * <p>
 * Created on 2/28/16.
 */
public class EmbeddedActorManager extends AbstractActorManager {

    private final ActorFactory factory;

    public EmbeddedActorManager(@NotNull final String agent, @NotNull final ActorFactory factory) {
        super(agent);
        this.factory = checkNotNull(factory);
    }

    @NotNull
    @Override
    public ManagedActor createActor(@NotNull final ActorConfig actorConfig) throws ValidationException {
        checkNotNull(actorConfig);

        final String name = actorConfig.getName();
        final Actor actor = factory.create(name, actorConfig.getClassName(), actorConfig.getActorJsonConfig());

        return new ManagedActor() {
            @NotNull
            @Override
            public String getName() {
                return name;
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
