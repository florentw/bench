package io.amaze.bench.client.runtime.actor;

import com.typesafe.config.Config;
import io.amaze.bench.client.api.IrrecoverableException;
import io.amaze.bench.client.api.MetricsCollector;
import io.amaze.bench.client.api.Sender;
import io.amaze.bench.client.api.TerminationException;
import io.amaze.bench.shared.metric.Metric;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/31/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
@io.amaze.bench.client.api.Actor
public final class TestActorMetrics extends TestActor {

    static final String PRODUCE_METRICS_MSG = "PRODUCE_METRICS_MSG";

    static final Metric DUMMY_METRIC_A = new Metric("latency", "ms", 1);
    static final Metric DUMMY_METRIC_B = new Metric("throughput", "events", "sec", 1);

    static final String DUMMY_METRIC_A_KEY = "DummyMetricA";
    static final String DUMMY_METRIC_B_KEY = "DummyMetricB";

    private final MetricsCollector metricsCollector;

    public TestActorMetrics(final Sender sender, final MetricsCollector metricsCollector) {
        super(sender);
        this.metricsCollector = metricsCollector;
    }

    public TestActorMetrics(final Sender sender, final MetricsCollector metricsCollector, final Config config) {
        super(sender, config);
        this.metricsCollector = metricsCollector;
    }

    @Override
    public void onMessage(@NotNull final String from, @NotNull final String message)
            throws IrrecoverableException, TerminationException {

        if (message.equals(PRODUCE_METRICS_MSG)) {
            metricsCollector.putMetric(DUMMY_METRIC_A_KEY, DUMMY_METRIC_A);
            metricsCollector.putMetric(DUMMY_METRIC_B_KEY, DUMMY_METRIC_B);
        }

        super.onMessage(from, message);
    }
}
