package io.amaze.bench.shared.metric;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 2/24/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class Metric implements Serializable {
    private final String label;
    private final String firstUnit;
    private final String secondUnit;
    private final Number value;

    public Metric(@NotNull final String label, @NotNull final String firstUnit, @NotNull final Number value) {
        this(label, firstUnit, null, value);
    }

    public Metric(@NotNull final String label,
                  @NotNull final String firstUnit,
                  @NotNull final String secondUnit,
                  @NotNull final Number value) {

        this.label = checkNotNull(label);
        this.firstUnit = checkNotNull(firstUnit);
        this.value = checkNotNull(value);
        this.secondUnit = secondUnit;
    }

    public String getLabel() {
        return label;
    }

    public Number getValue() {
        return value;
    }

    public String getSecondUnit() {
        return secondUnit;
    }

    public String getFirstUnit() {
        return firstUnit;
    }
}
