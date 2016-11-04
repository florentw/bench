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
package io.amaze.bench.runtime.actor;

import com.typesafe.config.Config;
import io.amaze.bench.api.ActorKey;
import io.amaze.bench.api.ReactorException;
import io.amaze.bench.api.Sender;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/31/16.
 */
@io.amaze.bench.api.Actor
public final class TestActorMetrics extends TestActor {

    static final String PRODUCE_METRICS_MSG = "PRODUCE_METRICS_MSG";

    static final Metric DUMMY_METRIC_A = Metric.metric("latency", "ms").build();
    static final Metric DUMMY_METRIC_B = Metric.metric("throughput", "events").secondUnit("seconds").build();

    private final Metrics metrics;

    public TestActorMetrics(final ActorKey actorKey, final Sender sender, final Metrics metrics, final Config config) {
        super(actorKey, sender, config);
        this.metrics = metrics;
    }

    protected TestActorMetrics(final Sender sender, final Metrics metrics) {
        super(sender);
        this.metrics = metrics;
    }

    @Override
    public void onMessage(@NotNull final String from, @NotNull final String message) throws ReactorException {

        if (message.equals(PRODUCE_METRICS_MSG)) {
            metrics.sinkFor(DUMMY_METRIC_A).add(10);
            metrics.sinkFor(DUMMY_METRIC_B).timed(1);
        }

        super.onMessage(from, message);
    }
}
