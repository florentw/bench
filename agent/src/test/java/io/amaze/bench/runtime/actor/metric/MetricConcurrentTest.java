package io.amaze.bench.runtime.actor.metric;

import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import io.amaze.bench.runtime.actor.TestActor;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MetricConcurrentTest {

    private static final int ITERATIONS = 50_000;
    private static final int WORKERS = 12;
    private static final int TEST_TIMEOUT = 30;

    private MetricsInternal metrics;
    private ExecutorService executorService;

    @Before
    public void before() {
        metrics = MetricsInternal.create(TestActor.DUMMY_ACTOR);
        executorService = Executors.newCachedThreadPool();
    }

    @Test
    public void creating_sinks_concurrently_does_not_hang() {
        runConcurrently(() -> metrics.sinkFor(randomMetric()));

        assertThat(metrics.getValues().size(), is(WORKERS * ITERATIONS));
    }

    @Test
    public void workers_add_concurrently_in_same_sink() {
        Metric metric = randomMetric();
        Metrics.Sink sink = metrics.sinkFor(metric);

        runConcurrently(() -> sink.add(1));

        assertThat(metrics.getValues().size(), is(1));
        assertThat(metrics.getValues().get(metric).size(), is(WORKERS * ITERATIONS));
    }

    @Test
    public void workers_add_timed_values_concurrently_in_same_sink() {
        Metric metric = randomMetric();
        Metrics.Sink sink = metrics.sinkFor(metric);

        runConcurrently(() -> sink.timed(1));

        assertThat(metrics.getValues().size(), is(1));
        assertThat(metrics.getValues().get(metric).size(), is(WORKERS * ITERATIONS));
    }

    private void runConcurrently(Runnable codeUnderTest) {
        CountDownLatch workersFinished = new CountDownLatch(WORKERS * ITERATIONS);

        for (int i = 0; i < WORKERS; i++) {
            createAndStartWorker(workersFinished, codeUnderTest);
        }

        assertTrue(awaitUninterruptibly(workersFinished, TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    private void createAndStartWorker(CountDownLatch workersFinished, Runnable codeUnderTest) {
        executorService.submit(() -> {
            for (int i = 0; i < ITERATIONS; i++) {
                codeUnderTest.run();
                workersFinished.countDown();
            }
        });
    }

    private static Metric randomMetric() {
        return Metric.metric(UUID.randomUUID().toString(), UUID.randomUUID().toString()).build();
    }

}
