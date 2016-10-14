package io.amaze.bench.runtime.cluster.jgroups;

import io.amaze.bench.runtime.cluster.registry.RegisteredAgent;

import java.io.Serializable;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/14/16.
 */
public final class AgentView implements Serializable {

    private final Set<RegisteredAgent> registeredAgents; // NOSONAR: It is serializable

    public AgentView(final Set<RegisteredAgent> registeredAgents) {
        this.registeredAgents = checkNotNull(registeredAgents);
    }

    public Set<RegisteredAgent> getRegisteredAgents() {
        return registeredAgents;
    }
}
