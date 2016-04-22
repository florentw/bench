package io.amaze.bench.client.runtime.actor;

import com.typesafe.config.Config;
import io.amaze.bench.client.api.ReactorException;
import io.amaze.bench.client.api.TerminationException;
import io.amaze.bench.client.api.actor.MetricsCollector;
import io.amaze.bench.client.api.actor.Sender;
import io.amaze.bench.shared.metric.Metric;
import org.jetbrains.annotations.NotNull;

/**
 * Created on 3/31/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
@io.amaze.bench.client.api.actor.Actor
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
    public void onMessage(@NotNull final String from,
                          @NotNull final String message) throws ReactorException, TerminationException {

        if (message.equals(PRODUCE_METRICS_MSG)) {
            metricsCollector.putMetric(DUMMY_METRIC_A_KEY, DUMMY_METRIC_A);
            metricsCollector.putMetric(DUMMY_METRIC_B_KEY, DUMMY_METRIC_B);
        }

        super.onMessage(from, message);
    }
}
