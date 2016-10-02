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

import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.runtime.agent.Constants;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryListener;
import io.amaze.bench.shared.jgroups.*;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/1/16.
 */
public final class JgroupsActorRegistryClusterClient implements ActorRegistryClusterClient {

    private static final JgroupsStateKey ACTOR_REGISTRY_STATE_KEY = new JgroupsStateKey(Constants.ACTOR_REGISTRY_TOPIC);

    private final JgroupsListenerMultiplexer listenerMultiplexer;
    private final JgroupsStateMultiplexer stateMultiplexer;
    private final ActorRegistry actorRegistry;

    public JgroupsActorRegistryClusterClient(@NotNull final JgroupsListenerMultiplexer listenerMultiplexer,
                                             @NotNull final JgroupsStateMultiplexer stateMultiplexer,
                                             @NotNull final ActorRegistry actorRegistry) {

        this.listenerMultiplexer = checkNotNull(listenerMultiplexer);
        this.stateMultiplexer = checkNotNull(stateMultiplexer);
        this.actorRegistry = checkNotNull(actorRegistry);
    }

    @Override
    public void startRegistryListener(@NotNull final ActorRegistryListener actorsListener) {
        listenerMultiplexer.addListener(ActorLifecycleMessage.class, registryListener(actorsListener));
        stateMultiplexer.addStateHolder(stateHolder());
    }

    @Override
    public void close() {
        listenerMultiplexer.removeListenerFor(ActorLifecycleMessage.class);
        stateMultiplexer.removeStateHolder(ACTOR_REGISTRY_STATE_KEY);
    }

    private JgroupsListener<ActorLifecycleMessage> registryListener(@NotNull final ActorRegistryListener actorsListener) {
        return (msg, lfMsg) -> {
            ActorKey actor = lfMsg.getActor();

            switch (lfMsg.getState()) {
                case CREATED:
                    actorsListener.onActorCreated(actor, lfMsg.getAgent());
                    break;
                case INITIALIZED:
                    actorsListener.onActorInitialized(actor, lfMsg.getDeployInfo());
                    break;
                case FAILED:
                    actorsListener.onActorFailed(actor, lfMsg.getThrowable());
                    break;
                case CLOSED:
                    actorsListener.onActorClosed(actor);
                    break;
                default:
            }
        };
    }

    private JgroupsStateHolder<ActorView> stateHolder() {
        return new JgroupsStateHolder<ActorView>() {
            @Override
            public JgroupsStateKey getKey() {
                return ACTOR_REGISTRY_STATE_KEY;
            }

            @Override
            public ActorView getState() {
                return new ActorView(actorRegistry.all());
            }

            @Override
            public void setState(@NotNull final ActorView newState) {
                checkNotNull(newState);
                actorRegistry.resetState(newState.getRegisteredActors());
            }
        };
    }
}
