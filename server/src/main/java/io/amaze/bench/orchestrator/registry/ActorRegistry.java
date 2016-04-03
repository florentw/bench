package io.amaze.bench.orchestrator.registry;

import io.amaze.bench.orchestrator.registry.RegisteredActor.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Thread safe
 * <p>
 * Created on 3/28/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class ActorRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ActorRegistry.class);

    private final Map<String, RegisteredActor> actors = new HashMap<>();
    private final Set<ActorRegistryListener> clientListeners = new HashSet<>();

    public void addListener(@NotNull final ActorRegistryListener listener) {
        synchronized (clientListeners) {
            clientListeners.add(listener);
        }
    }

    public void removeListener(@NotNull final ActorRegistryListener listener) {
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
        public void onActorStarted(@NotNull final String name, @NotNull final String agent) {
            synchronized (actors) {
                RegisteredActor found = actors.get(name);

                if (found != null) {
                    RegisteredActor newActor = new RegisteredActor(name, found.getAgent(), State.STARTED);
                    actors.put(name, newActor);

                } else {
                    LOG.warn("Attempt to start an unknown Actor: " + name);
                    return;
                }
            }

            // Notify listeners
            for (ActorRegistryListener listener : listeners()) {
                listener.onActorStarted(name, agent);
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
