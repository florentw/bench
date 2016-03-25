package io.amaze.bench.interpreter;

import java.util.concurrent.Callable;

/**
 * Created on 2/20/16
 *
 * @author Florent Weber
 */
public interface Step extends Callable<StepResult> {

    Object getKey();

    boolean equals(final Object o);

    int hashCode();
}
