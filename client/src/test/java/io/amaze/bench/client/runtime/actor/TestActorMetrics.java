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
package io.amaze.bench.client.runtime.actor;

import com.typesafe.config.Config;
import io.amaze.bench.api.IrrecoverableException;
import io.amaze.bench.api.MetricsCollector;
import io.amaze.bench.api.Sender;
import io.amaze.bench.api.TerminationException;
import io.amaze.bench.shared.metric.Metric;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/31/16.
 */
@io.amaze.bench.api.Actor
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
            metricsCollector.put(DUMMY_METRIC_A_KEY, DUMMY_METRIC_A);
            metricsCollector.put(DUMMY_METRIC_B_KEY, DUMMY_METRIC_B);
        }

        super.onMessage(from, message);
    }
}
