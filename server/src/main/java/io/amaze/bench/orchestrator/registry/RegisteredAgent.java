package io.amaze.bench.orchestrator.registry;

import io.amaze.bench.shared.metric.SystemInfo;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/29/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class RegisteredAgent {

    private final String name;
    private final SystemInfo systemInfo;
    private final long creationTime;

    RegisteredAgent(@NotNull final String name,
                    @NotNull final SystemInfo systemInfo,
                    @NotNull final long creationTime) {
        this.name = name;
        this.systemInfo = systemInfo;
        this.creationTime = creationTime;
    }

    public String getName() {
        return name;
    }

    public SystemInfo getSystemInfo() {
        return systemInfo;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
