package io.amaze.bench.client.runtime.agent;

import io.amaze.bench.shared.metric.SystemConfig;
import io.amaze.bench.shared.metric.SystemConfigs;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Message sent by an agent to the master after startup for registration.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class AgentRegistrationMessage implements Serializable {

    static final String DEFAULT_AGENT_PREFIX = "agent-";

    private final String name;
    private final SystemConfig systemConfig;
    private final long creationTime;

    public AgentRegistrationMessage(@NotNull final String name,
                                    @NotNull final SystemConfig systemConfig,
                                    final long creationTime) {
        this.name = checkNotNull(name);
        this.systemConfig = checkNotNull(systemConfig);
        this.creationTime = creationTime;
    }

    public static AgentRegistrationMessage create() {
        String name = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@", "-");
        return create(DEFAULT_AGENT_PREFIX + name);
    }

    public static AgentRegistrationMessage create(@NotNull final String name) {
        checkNotNull(name);

        SystemConfig systemConfig = SystemConfigs.get();
        long creationTime = System.currentTimeMillis();
        return new AgentRegistrationMessage(name, systemConfig, creationTime);
    }

    public String getName() {
        return name;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public SystemConfig getSystemConfig() {
        return systemConfig;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgentRegistrationMessage that = (AgentRegistrationMessage) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public String toString() {
        return "{\"AgentRegistrationMessage\":{" + //
                "\"name\":\"" + name + "\"" + ", " + //
                "\"systemConfig\":" + systemConfig + ", " + //
                "\"creationTime\":\"" + creationTime + "\"" + "}}";
    }
}
