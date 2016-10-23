/*
 * Copyright 2016 Florent Weber <florent.weber@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.amaze.bench.runtime.actor.metric;

import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import io.amaze.bench.runtime.actor.TestActor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;
import static io.amaze.bench.api.metric.Metric.metric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;

/**
 * Created on 9/10/16.
 */
public final class MetricsInternalConcurrencyTest {

    private static final int NB_THREADS = 16;
    private static final Metric DUMMY = metric("test", "sec").build();
    private static final int ITERATIONS = 25000;

    private final MetricsInternal metricsInternal = MetricsInternal.create(TestActor.DUMMY_ACTOR);
    private ExecutorService executorService;

    @Before
    public void initThreadPool() {
        executorService = Executors.newFixedThreadPool(NB_THREADS);
    }

    @After
    public void closeThreads() {
        executorService.shutdownNow();
    }

    @Test
    public void threads_requesting_sinks_for_same_metric() throws InterruptedException, ExecutionException {
        List<Callable<Metrics.Sink>> tasks = new ArrayList<>();
        for (int i = 0; i < NB_THREADS; i++) {
            tasks.add(() -> metricsInternal.sinkFor(DUMMY));
        }

        List<Future<Metrics.Sink>> futures = executorService.invokeAll(tasks);

        for (Future<Metrics.Sink> fSink : futures) {
            Metrics.Sink sink = getUninterruptibly(fSink);
            assertNotNull(sink);
        }
        assertThat(metricsInternal.dumpAndFlush().metrics().size(), is(1));
    }

    @Test
    public void threads_producing_metrics_in_same_sink() throws InterruptedException, ExecutionException {
        List<Callable<Void>> tasks = producerTasks();

        List<Future<Void>> futures = executorService.invokeAll(tasks);

        for (Future<Void> future : futures) {
            getUninterruptibly(future);
        }
        MetricValuesMessage message = metricsInternal.dumpAndFlush();
        assertThat(message.metrics().size(), is(1));
        assertThat(message.metrics().get(DUMMY).size(), is(NB_THREADS * ITERATIONS));
    }

    @Test
    public void threads_producing_metrics_in_same_sink_while_dumpAndFlush_happens()
            throws InterruptedException, ExecutionException {
        List<Callable<Void>> tasks = producerTasks();
        tasks.add(flusher());

        List<Future<Void>> futures = executorService.invokeAll(tasks);

        for (Future<Void> future : futures) {
            getUninterruptibly(future);
        }
    }

    private List<Callable<Void>> producerTasks() {
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < NB_THREADS; i++) {
            tasks.add(producer());
        }
        return tasks;
    }

    private Callable<Void> producer() {
        return () -> {
            for (int j = 0; j < ITERATIONS; j++) {
                Metrics.Sink sink = metricsInternal.sinkFor(DUMMY);
                sink.add(j);
            }
            return null;
        };
    }

    private Callable<Void> flusher() {
        return () -> {
            for (int j = 0; j < ITERATIONS; j++) {
                metricsInternal.dumpAndFlush();
            }
            return null;
        };
    }

}
