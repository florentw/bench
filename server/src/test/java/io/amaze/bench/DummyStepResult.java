package io.amaze.bench;

import io.amaze.bench.interpreter.StepResult;

public final class DummyStepResult implements StepResult {

    private final String stepKey;

    public DummyStepResult(String stepKey) {
        this.stepKey = stepKey;
    }

    public String getStepKey() {
        return stepKey;
    }

    @Override
    public String toString() {
        return "DummyStepResult{" +
                "stepKey='" + stepKey + '\'' +
                '}';
    }
}
