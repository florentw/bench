package io.amaze.bench.shared.metric;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Map;

/**
 * Created on 3/20/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class ProcessorInfo implements Serializable {

    private final String modelName;

    private final int cores;
    private final String frequency;
    private final String cacheSize;

    private final Map<String, String> properties;

    ProcessorInfo(@NotNull final String modelName,
                  @NotNull final int cores,
                  @NotNull final String frequency,
                  @NotNull final String cacheSize,
                  @NotNull final Map<String, String> properties) {

        this.modelName = modelName;
        this.cores = cores;
        this.frequency = frequency;
        this.cacheSize = cacheSize;
        this.properties = properties;
    }

    public String getModelName() {
        return modelName;
    }

    public int getCores() {
        return cores;
    }

    public String getCacheSize() {
        return cacheSize;
    }

    public String getFrequency() {
        return frequency;
    }

    public Map<String, String> getProperties() {
        return ImmutableMap.copyOf(properties);
    }

}
