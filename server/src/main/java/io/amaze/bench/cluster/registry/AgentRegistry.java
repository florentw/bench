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
package io.amaze.bench.cluster.registry;

import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/28/16.
 */
public class AgentRegistry {

    private static final Logger LOG = LogManager.getLogger(AgentRegistry.class);

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
                LOG.warn("Attempt to remove unknown agent listener {}", listener);
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
    public AgentRegistryListener createClusterListener() {
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
                    LOG.warn("Attempt to remove unknown agent {}", agent);
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
