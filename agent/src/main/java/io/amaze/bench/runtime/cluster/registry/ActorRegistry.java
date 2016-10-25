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
package io.amaze.bench.runtime.cluster.registry;

import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.actor.ActorDeployInfo;
import io.amaze.bench.runtime.actor.ActorKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Thread safe
 */
public class ActorRegistry {

    private static final Logger log = LogManager.getLogger();

    private final Map<ActorKey, RegisteredActor> actors = new HashMap<>();
    private final Set<ActorRegistryListener> clientListeners = new HashSet<>();

    public void resetState(@NotNull final Set<RegisteredActor> initialActorSet) {
        checkNotNull(initialActorSet);
        synchronized (actors) {
            actors.clear();
            for (RegisteredActor actor : initialActorSet) {
                actors.put(actor.getKey(), actor);
            }
        }
    }

    /**
     * Registers a listener that will be notified with actors lifecycle events.
     *
     * @param listener The listener to be notified of events
     * @throws IllegalStateException if the listener is already registered
     */
    public void addListener(@NotNull final ActorRegistryListener listener) {
        checkNotNull(listener);

        synchronized (clientListeners) {
            if (clientListeners.contains(listener)) {
                throw new IllegalStateException("Registry listener already is registered.");
            }

            clientListeners.add(listener);
        }
    }

    /**
     * Unregisters a previously registered listener, it will no longer be notified of lifecycle events.
     * If the listener is already unregistered, no exception is thrown.
     *
     * @param listener The listener instance to unregister
     */
    public void removeListener(@NotNull final ActorRegistryListener listener) {
        checkNotNull(listener);

        synchronized (clientListeners) {
            boolean removed = clientListeners.remove(listener);
            if (!removed) {
                log.warn("Attempt to remove unknown actor listener {}", listener);
            }
        }
    }

    /**
     * @param key key of the actor to find
     * @return {@code null} if no actor with that name is found, returns the actor otherwise
     */
    public RegisteredActor byKey(@NotNull final ActorKey key) {
        checkNotNull(key);

        synchronized (actors) {
            return actors.get(key);
        }
    }

    /**
     * @return An unmodifiable set of currently registered actors.
     */
    public Set<RegisteredActor> all() {
        synchronized (actors) {
            return Collections.unmodifiableSet(new HashSet<>(actors.values()));
        }
    }

    /**
     * To be called by the messaging system in case a disconnection is detected.
     *
     * @param endpoint The endpoint of the member that left the cluster.
     */
    public void onEndpointDisconnected(final Endpoint endpoint) {
        checkNotNull(endpoint);

        List<ActorKey> actorsThatLeft = new ArrayList<>();
        synchronized (actors) {
            Stream<RegisteredActor> filteredOnEndpoint = actors.values().stream().filter(actor -> {
                ActorDeployInfo deployInfo = actor.getDeployInfo();
                return deployInfo != null && //
                        deployInfo.getEndpoint().equals(endpoint);
            });
            List<ActorKey> collect = filteredOnEndpoint.map(RegisteredActor::getKey).collect(Collectors.toList());
            actorsThatLeft.addAll(collect);

            if (!actorsThatLeft.isEmpty()) {
                log.info("Detected actor disconnection for {}.", actorsThatLeft);
                actorsThatLeft.forEach(actors::remove);
            } else {
                return;
            }
        }

        // Notify listeners
        for (ActorRegistryListener listener : listeners()) {
            actorsThatLeft.forEach(actorKey -> listener.onActorFailed(actorKey, new ActorDisconnectedException()));
        }
    }

    @NotNull
    public ActorRegistryListener createClusterListener() {
        return new ActorRegistryListenerLogger(new ActorRegistryListenerState());
    }

    private Set<ActorRegistryListener> listeners() {
        synchronized (clientListeners) {
            return new HashSet<>(clientListeners);
        }
    }

    private final class ActorRegistryListenerState implements ActorRegistryListener {

        @Override
        public void onActorCreated(@NotNull final ActorKey key, @NotNull final String agent) {
            synchronized (actors) {
                if (actors.containsKey(key)) {
                    log.warn("An actor {} already exists for key {}", actors.get(key), key);
                    return;
                }
                actors.put(key, RegisteredActor.created(key, agent));
            }

            // Notify listeners
            for (ActorRegistryListener listener : listeners()) {
                listener.onActorCreated(key, agent);
            }
        }

        @Override
        public void onActorInitialized(@NotNull final ActorKey actorKey, @NotNull final ActorDeployInfo deployInfo) {
            synchronized (actors) {
                RegisteredActor foundActor = actors.remove(actorKey);
                if (foundActor == null || foundActor.getState() == RegisteredActor.State.INITIALIZED) {
                    log.warn("Attempt to initialize an unknown Actor: {}", actorKey);
                    return;
                }

                RegisteredActor initialized = RegisteredActor.initialized(foundActor, deployInfo);
                actors.put(actorKey, initialized);
            }

            // Notify listeners
            for (ActorRegistryListener listener : listeners()) {
                listener.onActorInitialized(actorKey, deployInfo);
            }
        }

        @Override
        public void onActorFailed(@NotNull final ActorKey name, @NotNull final Throwable throwable) {
            synchronized (actors) {
                RegisteredActor removed = actors.remove(name);
                if (removed == null) {
                    log.warn("Attempt to remove for failure an unknown actor: {}", name, throwable);
                    return;
                }
            }

            // Notify listeners
            for (ActorRegistryListener listener : listeners()) {
                listener.onActorFailed(name, throwable);
            }
        }

        @Override
        public void onActorClosed(@NotNull final ActorKey name) {
            synchronized (actors) {
                RegisteredActor removed = actors.remove(name);
                if (removed == null) {
                    log.warn("Attempt to close an unknown actor: {}", name);
                    return;
                }
            }

            // Notify listeners
            for (ActorRegistryListener listener : listeners()) {
                listener.onActorClosed(name);
            }
        }
    }
}
