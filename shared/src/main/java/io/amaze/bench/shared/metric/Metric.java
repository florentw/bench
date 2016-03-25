package io.amaze.bench.shared.metric;

import java.io.Serializable;

/**
 * Created on 2/24/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class Metric implements Serializable {
    private final String label;
    private final String firstUnit;
    private final String secondUnit;
    private final Number value;

    public Metric(final String label, final String firstUnit, final String secondUnit, final Number value) {
        this.label = label;
        this.firstUnit = firstUnit;
        this.secondUnit = secondUnit;
        this.value = value;
    }

    public Metric(final String label, final String firstUnit, final Number value) {
        this.label = label;
        this.firstUnit = firstUnit;
        this.secondUnit = null;
        this.value = value;
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
