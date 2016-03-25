package io.amaze.bench.shared.metric;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * Created on 3/20/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class MemoryInfo implements Serializable {

    private final long totalMemoryKb;
    private final Map<String, String> memoryProperties;

    public MemoryInfo(final long totalMemoryKb, @NotNull final Map<String, String> memoryProperties) {
        this.totalMemoryKb = totalMemoryKb;
        this.memoryProperties = memoryProperties;
    }

    public long getTotalMemoryKb() {
        return totalMemoryKb;
    }

    public Map<String, String> getMemoryProperties() {
        return memoryProperties;
    }
}
