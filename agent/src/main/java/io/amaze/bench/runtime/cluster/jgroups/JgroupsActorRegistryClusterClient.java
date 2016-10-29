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

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.runtime.cluster.actor.ActorKey;
import io.amaze.bench.runtime.cluster.actor.ActorLifecycleMessage;
import io.amaze.bench.runtime.cluster.agent.Constants;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryListener;
import io.amaze.bench.shared.jgroups.*;
import org.jgroups.Address;
import org.jgroups.Message;

import javax.validation.constraints.NotNull;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/1/16.
 */
public class JgroupsActorRegistryClusterClient implements ActorRegistryClusterClient {

    static final JgroupsStateKey ACTOR_REGISTRY_STATE_KEY = new JgroupsStateKey(Constants.ACTOR_REGISTRY_TOPIC);

    private final JgroupsListenerMultiplexer listenerMultiplexer;
    private final JgroupsStateMultiplexer stateMultiplexer;
    private final JgroupsViewMultiplexer viewMultiplexer;
    private final ActorRegistry actorRegistry;
    private JgroupsViewListener viewListener;
    private JgroupsListener<ActorLifecycleMessage> listener;

    JgroupsActorRegistryClusterClient(@NotNull final JgroupsListenerMultiplexer listenerMultiplexer,
                                      @NotNull final JgroupsStateMultiplexer stateMultiplexer,
                                      @NotNull final JgroupsViewMultiplexer viewMultiplexer,
                                      @NotNull final ActorRegistry actorRegistry) {

        this.listenerMultiplexer = checkNotNull(listenerMultiplexer);
        this.stateMultiplexer = checkNotNull(stateMultiplexer);
        this.actorRegistry = checkNotNull(actorRegistry);
        this.viewMultiplexer = checkNotNull(viewMultiplexer);

        stateMultiplexer.addStateHolder(stateHolder());
    }

    @Override
    public void startRegistryListener(@NotNull final ActorRegistryListener actorsListener) {
        checkNotNull(actorsListener);

        listener = registryListener(actorsListener);
        listenerMultiplexer.addListener(ActorLifecycleMessage.class, listener);
        viewListener = viewListener();
        viewMultiplexer.addListener(viewListener);
    }

    @Override
    public void close() {
        stateMultiplexer.removeStateHolder(ACTOR_REGISTRY_STATE_KEY);

        // to be done only if startRegistryListener was called
        if (viewListener != null) {
            listenerMultiplexer.removeListener(listener);
            viewMultiplexer.removeListener(viewListener);
        }
    }

    private JgroupsViewListener viewListener() {
        return new ActorRegistryViewListener(actorRegistry);
    }

    private JgroupsListener<ActorLifecycleMessage> registryListener(@NotNull final ActorRegistryListener actorsListener) {
        return new RegistryMessageListener(actorsListener);
    }

    private JgroupsStateHolder<ActorView> stateHolder() {
        return new RegistryStateHolder(actorRegistry);
    }

    @VisibleForTesting
    static final class RegistryMessageListener implements JgroupsListener<ActorLifecycleMessage> {
        private final ActorRegistryListener actorsListener;

        RegistryMessageListener(@NotNull final ActorRegistryListener actorsListener) {
            this.actorsListener = actorsListener;
        }

        @Override
        public void onMessage(@NotNull final Message msg, @NotNull final ActorLifecycleMessage lfMsg) {
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
        }
    }

    @VisibleForTesting
    static final class ActorRegistryViewListener implements JgroupsViewListener {
        private final ActorRegistry actorRegistry;

        ActorRegistryViewListener(@NotNull final ActorRegistry actorRegistry) {
            this.actorRegistry = actorRegistry;
        }

        @Override
        public void initialView(final Collection<Address> members) {
            // Nothing to do here
        }

        @Override
        public void memberJoined(@NotNull final Address address) {
            // Nothing to do here
        }

        @Override
        public void memberLeft(@NotNull final Address address) {
            actorRegistry.onEndpointDisconnected(new JgroupsEndpoint(address));
        }
    }

    @VisibleForTesting
    static final class RegistryStateHolder implements JgroupsStateHolder<ActorView> {
        private final ActorRegistry actorRegistry;

        RegistryStateHolder(@NotNull final ActorRegistry actorRegistry) {
            this.actorRegistry = actorRegistry;
        }

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
    }
}
