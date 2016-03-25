package io.amaze.bench;

import io.amaze.bench.interpreter.Step;
import io.amaze.bench.interpreter.StepResult;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public final class DummyStep implements Step {

    private final String key;
    private final boolean forceFail;
    private int nbCalled = 0;

    public DummyStep(String key) {
        this.key = key;
        forceFail = false;
    }

    public DummyStep(String key, boolean forceFail) {
        this.key = key;
        this.forceFail = forceFail;
    }

    @Override
    public StepResult call() throws Exception {
        if (forceFail) {
            throw new ExecutionException("Failure provoked for tests", new Exception());
        }
        nbCalled++;
        return new DummyStepResult(key);
    }

    @Override
    public Object getKey() {
        return key;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DummyStep dummyStep = (DummyStep) o;
        return Objects.equals(key, dummyStep.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "DummyStep{" +
                "key='" + key + '\'' +
                '}';
    }

    public int getNbCalled() {
        return nbCalled;
    }
}