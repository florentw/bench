package io.amaze.bench.client.runtime.agent;

import io.amaze.bench.shared.metric.SystemInfo;
import io.amaze.bench.shared.metric.SystemInfos;

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
public class AgentRegistrationMessage implements Serializable {

    static final String DEFAULT_AGENT_PREFIX = "agent-";

    private final String name;
    private final SystemInfo systemInfo;
    private final long creationTime;

    private AgentRegistrationMessage(@NotNull final String name,
                                     @NotNull final SystemInfo systemInfo,
                                     final long creationTime) {
        this.name = checkNotNull(name);
        this.systemInfo = checkNotNull(systemInfo);
        this.creationTime = creationTime;
    }

    public static AgentRegistrationMessage create() {
        String name = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@", "-");
        return create(DEFAULT_AGENT_PREFIX + name);
    }

    public static AgentRegistrationMessage create(@NotNull final String name) {
        checkNotNull(name);

        SystemInfo systemInfo = SystemInfos.get();
        long creationTime = System.currentTimeMillis();
        return new AgentRegistrationMessage(name, systemInfo, creationTime);
    }

    public String getName() {
        return name;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public SystemInfo getSystemInfo() {
        return systemInfo;
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
        return "AgentRegistrationMessage{" + "name='" + name + '\'' + ", systemInfo=" + systemInfo + ", creationTime=" + creationTime + '}';
    }
}
