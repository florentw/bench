package io.amaze.bench.orchestrator.registry;

import io.amaze.bench.shared.metric.SystemConfig;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/29/16.
 */
public final class RegisteredAgent {

    private final String name;
    private final SystemConfig systemConfig;
    private final long creationTime;

    RegisteredAgent(@NotNull final String name, @NotNull final SystemConfig systemConfig,
                    @NotNull final long creationTime) {

        this.name = checkNotNull(name);
        this.systemConfig = checkNotNull(systemConfig);
        this.creationTime = checkNotNull(creationTime);
    }

    public String getName() {
        return name;
    }

    public SystemConfig getSystemConfig() {
        return systemConfig;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
