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
package io.amaze.bench.cluster.jgroups;

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.cluster.actor.ActorLifecycleMessage;
import io.amaze.bench.cluster.agent.Constants;
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.cluster.registry.ActorRegistryListener;
import io.amaze.bench.shared.jgroups.*;
import org.jgroups.Address;
import org.jgroups.Message;

import javax.validation.constraints.NotNull;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

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

        this.listenerMultiplexer = requireNonNull(listenerMultiplexer);
        this.stateMultiplexer = requireNonNull(stateMultiplexer);
        this.actorRegistry = requireNonNull(actorRegistry);
        this.viewMultiplexer = requireNonNull(viewMultiplexer);

        stateMultiplexer.addStateHolder(stateHolder());
    }

    @Override
    public void startRegistryListener(@NotNull final ActorRegistryListener actorsListener) {
        requireNonNull(actorsListener);

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
        public void onMessage(@NotNull final Message msg, @NotNull final ActorLifecycleMessage lifecycleMessage) {
            requireNonNull(msg);
            requireNonNull(lifecycleMessage);

            lifecycleMessage.sendTo(actorsListener);
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
            requireNonNull(newState);
            actorRegistry.resetState(newState.registeredActors());
        }
    }
}
