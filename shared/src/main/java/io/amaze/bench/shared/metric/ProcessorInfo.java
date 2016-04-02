package io.amaze.bench.shared.metric;

import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProcessorInfo that = (ProcessorInfo) o;
        return cores == that.cores &&
                Objects.equals(modelName, that.modelName) &&
                Objects.equals(frequency, that.frequency) &&
                Objects.equals(cacheSize, that.cacheSize) &&
                Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelName, cores, frequency, cacheSize, properties);
    }
}
