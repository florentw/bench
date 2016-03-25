package io.amaze.bench.client.api.actor;


import io.amaze.bench.shared.metric.Metric;

import javax.validation.constraints.NotNull;

/**
 * Created on 2/28/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface MetricsCollector {

    void putMetric(@NotNull String key, @NotNull Metric metric);

}
