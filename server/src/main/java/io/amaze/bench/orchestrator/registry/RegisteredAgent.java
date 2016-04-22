package io.amaze.bench.orchestrator.registry;

import io.amaze.bench.shared.metric.SystemInfo;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

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

        this.name = checkNotNull(name);
        this.systemInfo = checkNotNull(systemInfo);
        this.creationTime = checkNotNull(creationTime);
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
