package io.amaze.bench.shared.metric;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/20/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class MemoryConfig implements Serializable {

    private final long totalMemoryKb;
    private final Map<String, String> memoryProperties;

    public MemoryConfig(final long totalMemoryKb, @NotNull final Map<String, String> memoryProperties) {
        this.totalMemoryKb = totalMemoryKb;
        this.memoryProperties = checkNotNull(memoryProperties);
    }

    public long getTotalMemoryKb() {
        return totalMemoryKb;
    }

    public Map<String, String> getMemoryProperties() {
        return memoryProperties;
    }

    @Override
    public String toString() {
        return "{\"MemoryConfig\":{" + //
                "\"totalMemoryKb\":\"" + totalMemoryKb + "\"" + ", " + //
                "\"memoryProperties\":\"" + memoryProperties + "\"}}";
    }
}
