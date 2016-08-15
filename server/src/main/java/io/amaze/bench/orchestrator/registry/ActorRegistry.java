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
package io.amaze.bench.orchestrator.registry;

import io.amaze.bench.orchestrator.registry.RegisteredActor.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Thread safe
 * <p>
 * Created on 3/28/16.
 */
public final class ActorRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ActorRegistry.class);

    private final Map<String, RegisteredActor> actors = new HashMap<>();
    private final Set<ActorRegistryListener> clientListeners = new HashSet<>();

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
                LOG.warn("Attempt to remove unknown actor listener " + listener);
            }
        }
    }

    @NotNull
    public ActorRegistryListener getListenerForOrchestrator() {
        return new ActorRegistryListenerLogger(new ActorRegistryListenerState());
    }

    /**
     * @param name name of the actor to find
     * @return null if no actor with that name is found, returns the actor otherwise
     */
    public RegisteredActor byName(@NotNull final String name) {
        checkNotNull(name);

        synchronized (actors) {
            return actors.get(name);
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
        public void onActorCreated(@NotNull final String name, @NotNull final String agent) {
            synchronized (actors) {
                actors.put(name, new RegisteredActor(name, agent, State.CREATED));
            }

            // Notify listeners
            for (ActorRegistryListener listener : listeners()) {
                listener.onActorCreated(name, agent);
            }
        }

        @Override
        public void onActorInitialized(@NotNull final String name, @NotNull final String agent) {
            synchronized (actors) {
                RegisteredActor found = actors.get(name);

                if (found != null) {
                    RegisteredActor newActor = new RegisteredActor(name, found.getAgent(), State.INITIALIZED);
                    actors.put(name, newActor);

                } else {
                    LOG.warn("Attempt to initialize an unknown Actor: " + name);
                    return;
                }
            }

            // Notify listeners
            for (ActorRegistryListener listener : listeners()) {
                listener.onActorInitialized(name, agent);
            }
        }

        @Override
        public void onActorFailed(@NotNull final String name, @NotNull final Throwable throwable) {
            synchronized (actors) {
                RegisteredActor removed = actors.remove(name);
                if (removed == null) {
                    LOG.warn("Attempt to remove for failure an unknown actor: " + name, throwable);
                    return;
                }
            }

            // Notify listeners
            for (ActorRegistryListener listener : listeners()) {
                listener.onActorFailed(name, throwable);
            }
        }

        @Override
        public void onActorClosed(@NotNull final String name) {
            synchronized (actors) {
                RegisteredActor removed = actors.remove(name);
                if (removed == null) {
                    LOG.warn("Attempt to close an unknown actor: " + name);
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
