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

import io.amaze.bench.runtime.actor.ActorDeployInfo;
import io.amaze.bench.runtime.actor.ActorKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Thread safe
 */
public class ActorRegistry {

    private static final Logger log = LogManager.getLogger();

    private final Map<ActorKey, RegisteredActor> actors = new HashMap<>();
    private final Set<ActorRegistryListener> clientListeners = new HashSet<>();

    public void resetState(final Set<RegisteredActor> initialActorSet) {
        checkNotNull(initialActorSet);
        synchronized (actors) {
            actors.clear();
            for (RegisteredActor actor : initialActorSet) {
                actors.put(actor.getKey(), actor);
            }
        }
    }

    public void addListener(@NotNull final ActorRegistryListener listener) {
        checkNotNull(listener);

        synchronized (clientListeners) {
            clientListeners.add(listener);
        }
    }

    public void removeListener(@NotNull final ActorRegistryListener listener) {
        checkNotNull(listener);

        synchronized (clientListeners) {
            boolean removed = clientListeners.remove(listener);
            if (!removed) {
                log.warn("Attempt to remove unknown actor listener {}", listener);
            }
        }
    }

    @NotNull
    public ActorRegistryListener createClusterListener() {
        return new ActorRegistryListenerLogger(new ActorRegistryListenerState());
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

    private final class ActorRegistryListenerState implements ActorRegistryListener {

        @Override
        public void onActorCreated(@NotNull final ActorKey key, @NotNull final String agent) {
            synchronized (actors) {
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

        private Set<ActorRegistryListener> listeners() {
            synchronized (clientListeners) {
                return new HashSet<>(clientListeners);
            }
        }
    }
}