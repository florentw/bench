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
package io.amaze.bench.cluster;

import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import io.amaze.bench.client.runtime.actor.metric.MetricValuesMessage;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Repository for metric values produced by actors.<br/>
 * It offers the ability to get all previously produced actors metric values using {@link #allValues()}, or to wait for
 * an actor to produce values using {@link #expectValuesFor(String)}.
 *
 * @see MetricValuesMessage
 * @see Metric
 * @see Metrics
 */
public interface MetricsRepository {

    /**
     * Returns the produced metric values of the specified actor if any.
     *
     * @param actor Actor to get metric values for.
     * @return Produced metric values for the given actor or {@code null}
     */
    MetricValuesMessage valuesFor(@NotNull String actor);

    /**
     * Returns a {@link Future} that will be set once the specified actor has produced values.
     * If the actor has already produced metric values, the future will be set right away.
     *
     * @param actor Actor to get metrics for.
     * @return A future of {@link MetricValuesMessage}.
     */
    Future<MetricValuesMessage> expectValuesFor(@NotNull String actor);

    /**
     * Returns produced metric values produced until now by actors.
     *
     * @return A map of produced metric values with actor names as the key.
     */
    Map<String, MetricValuesMessage> allValues();

}
