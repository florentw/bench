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
package io.amaze.bench.cluster.registry;

import io.amaze.bench.cluster.Endpoint;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.agent.AgentRegistrationMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/28/16.
 */
public class AgentRegistry {

    private static final Logger log = LogManager.getLogger();

    private final Map<AgentKey, RegisteredAgent> agents = new HashMap<>();
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
                log.warn("Attempt to remove unknown agent listener {}", listener);
            }
        }
    }

    public Set<RegisteredAgent> all() {
        synchronized (agents) {
            return Collections.unmodifiableSet(new HashSet<>(agents.values()));
        }
    }

    public RegisteredAgent byKey(@NotNull final AgentKey agentKey) {
        checkNotNull(agentKey);

        synchronized (agents) {
            return agents.get(agentKey);
        }
    }

    @NotNull
    public AgentRegistryListener createClusterListener() {
        return new AgentRegistryListenerLogger(new AgentLifecycleListener());
    }

    public void resetState(@NotNull final Set<RegisteredAgent> initialAgents) {
        checkNotNull(initialAgents);
        synchronized (agents) {
            agents.clear();
            for (RegisteredAgent agent : initialAgents) {
                agents.put(agent.getAgentKey(), agent);
            }
        }
    }

    public void onEndpointDisconnected(final Endpoint endpoint) {
        checkNotNull(endpoint);
        List<AgentKey> agentsThatLeft = new ArrayList<>();
        synchronized (agents) {
            Predicate<RegisteredAgent> endpointFilter = agent -> agent.getEndpoint().equals(endpoint);
            Stream<RegisteredAgent> filtered = agents.values().stream().filter(endpointFilter);
            agentsThatLeft.addAll(filtered.map(RegisteredAgent::getAgentKey).collect(Collectors.toList()));

            if (!agentsThatLeft.isEmpty()) {
                log.info("Detected agent disconnection for {}.", agentsThatLeft);
                agentsThatLeft.forEach(agents::remove);
            } else {
                return;
            }
        }

        // Notify listeners
        for (AgentRegistryListener listener : listeners()) {
            agentsThatLeft.forEach(agent -> listener.onAgentFailed(agent, new AgentDisconnectedException()));
        }
    }

    private Set<AgentRegistryListener> listeners() {
        synchronized (clientListeners) {
            return new HashSet<>(clientListeners);
        }
    }

    private final class AgentLifecycleListener implements AgentRegistryListener {

        @Override
        public void onAgentRegistration(@NotNull final AgentRegistrationMessage msg) {
            synchronized (agents) {
                RegisteredAgent agent = new RegisteredAgent(msg.getKey(),
                                                            msg.getSystemConfig(),
                                                            msg.getCreationTime(),
                                                            msg.getEndpoint());
                agents.put(msg.getKey(), agent);
            }

            for (AgentRegistryListener listener : listeners()) {
                listener.onAgentRegistration(msg);
            }
        }

        @Override
        public void onAgentSignOff(@NotNull final AgentKey agent) {
            if (!removeAgent(agent)) {
                return;
            }

            for (AgentRegistryListener listener : listeners()) {
                listener.onAgentSignOff(agent);
            }
        }

        @Override
        public void onAgentFailed(final AgentKey agent, final Throwable throwable) {
            if (!removeAgent(agent)) {
                return;
            }

            for (AgentRegistryListener listener : listeners()) {
                listener.onAgentFailed(agent, throwable);
            }
        }

        private boolean removeAgent(final AgentKey agent) {
            synchronized (agents) {
                RegisteredAgent removed = agents.remove(agent);
                if (removed == null) {
                    log.warn("Attempt to remove unknown agent {}", agent);
                    return false;
                }
            }
            return true;
        }
    }
}
