package io.amaze.bench.interpreter;

import java.util.concurrent.ExecutionException;

/**
 * Created on 2/21/16
 *
 * @author Florent Weber
 */
public interface StepResultsSink {
    void onResult(final StepResult result);

    void onException(Step step, ExecutionException e);
}
