package io.amaze.bench.orchestrator.registry;

import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/28/16.
 */
public final class AgentRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(AgentRegistry.class);

    private final Map<String, RegisteredAgent> agents = new HashMap<>();
    private final Set<AgentRegistryListener> clientListeners = new HashSet<>();

    public void addListener(@NotNull final AgentRegistryListener listener) {
        checkNotNull(listener);

        synchronized (clientListeners) {
            clientListeners.add(listener);
        }
    }

    public void removeListener(@NotNull final AgentRegistryListener listener) {
        checkNotNull(listener);

        synchronized (clientListeners) {
            boolean removed = clientListeners.remove(listener);
            if (!removed) {
                LOG.warn("Attempt to remove unknown agent listener " + listener);
            }
        }
    }

    public Set<RegisteredAgent> all() {
        synchronized (agents) {
            return Collections.unmodifiableSet(new HashSet<>(agents.values()));
        }
    }

    public RegisteredAgent byName(@NotNull final String name) {
        checkNotNull(name);

        synchronized (agents) {
            return agents.get(name);
        }
    }

    @NotNull
    public AgentRegistryListener getListenerForOrchestrator() {
        return new AgentRegistryListenerLogger(new AgentRegistryState());
    }

    private final class AgentRegistryState implements AgentRegistryListener {

        @Override
        public void onAgentRegistration(@NotNull final AgentRegistrationMessage msg) {
            synchronized (agents) {
                RegisteredAgent agent = new RegisteredAgent(msg.getName(),
                                                            msg.getSystemConfig(),
                                                            msg.getCreationTime());
                agents.put(msg.getName(), agent);
            }

            for (AgentRegistryListener listener : listeners()) {
                listener.onAgentRegistration(msg);
            }
        }

        @Override
        public void onAgentSignOff(@NotNull final String agent) {
            synchronized (agents) {
                RegisteredAgent removed = agents.remove(agent);
                if (removed == null) {
                    LOG.warn("Attempt to remove unknown agent " + agent);
                    return;
                }
            }

            for (AgentRegistryListener listener : listeners()) {
                listener.onAgentSignOff(agent);
            }
        }

        private Set<AgentRegistryListener> listeners() {
            synchronized (clientListeners) {
                return new HashSet<>(clientListeners);
            }
        }
    }
}
