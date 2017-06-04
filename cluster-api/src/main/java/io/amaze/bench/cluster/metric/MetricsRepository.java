/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
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
package io.amaze.bench.cluster.metric;

import com.google.common.util.concurrent.SettableFuture;
import io.amaze.bench.api.ActorKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.Future;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of the MetricsRepository (aggregate incoming metrics from actors).
 */
public class MetricsRepository {

    private static final Logger log = LogManager.getLogger();

    private final Map<ActorKey, MetricValuesMessage> actorValues = new HashMap<>();
    private final Map<ActorKey, List<SettableFuture<MetricValuesMessage>>> expectedActors = new HashMap<>();

    /**
     * Returns the produced metric values of the specified actor if any.
     *
     * @param actor Actor to get metric values for.
     * @return Produced metric values for the given actor or {@code null}
     */
    public MetricValuesMessage valuesFor(@NotNull final ActorKey actor) {
        requireNonNull(actor);
        synchronized (actorValues) {
            return actorValues.get(actor);
        }
    }

    /**
     * Returns a {@link Future} that will be set once the specified actor has produced values.
     * If the actor has already produced metric values, the future will be set right away.
     *
     * @param actor Actor to get metrics for.
     * @return A future of {@link MetricValuesMessage}.
     */
    public Future<MetricValuesMessage> expectValuesFor(@NotNull final ActorKey actor) {
        requireNonNull(actor);
        synchronized (actorValues) {
            Optional<Future<MetricValuesMessage>> immediate = immediateResult(actor);
            return immediate.orElseGet(() -> registerExpectedActor(actor));
        }
    }

    /**
     * Returns produced metric values produced until now by actors.
     *
     * @return A map of produced metric values with actor names as the key.
     */
    public Map<ActorKey, MetricValuesMessage> allValues() {
        synchronized (actorValues) {
            return new HashMap<>(actorValues); // No need to copy nested objects, they are immutable.
        }
    }

    @NotNull
    public MetricsRepositoryListener createClusterListener() {
        return metrics -> {
            ActorKey from = metrics.fromActor();
            log.info("Received metric values from {}: {}", from, metrics);

            synchronized (actorValues) {
                MetricValuesMessage currentActorMetrics = updateActorMetricValues(from, metrics);
                setExpectedActorFutures(from, currentActorMetrics);
            }
        };
    }

    private void setExpectedActorFutures(final ActorKey actor, final MetricValuesMessage currentActorMetrics) {
        List<SettableFuture<MetricValuesMessage>> futures = expectedActors.remove(actor);
        if (futures != null) {
            for (SettableFuture<MetricValuesMessage> future : futures) {
                future.set(currentActorMetrics);
            }
        }
    }

    private MetricValuesMessage updateActorMetricValues(final ActorKey actor, final MetricValuesMessage metrics) {
        MetricValuesMessage currentActorMetrics = actorValues.remove(actor);
        if (currentActorMetrics != null) {
            currentActorMetrics = currentActorMetrics.mergeWith(metrics);
        } else {
            currentActorMetrics = metrics;
        }
        actorValues.put(actor, currentActorMetrics);
        return currentActorMetrics;
    }

    private SettableFuture<MetricValuesMessage> registerExpectedActor(final ActorKey actor) {
        List<SettableFuture<MetricValuesMessage>> futures = expectedActors.get(actor);
        SettableFuture<MetricValuesMessage> future = SettableFuture.create();
        if (futures != null) {
            futures.add(future);
        } else {
            List<SettableFuture<MetricValuesMessage>> list = new ArrayList<>();
            list.add(future);
            expectedActors.put(actor, list);
        }
        return future;
    }

    private Optional<Future<MetricValuesMessage>> immediateResult(final ActorKey actor) {
        MetricValuesMessage metricValues = actorValues.get(actor);
        if (metricValues == null) {
            return Optional.empty();
        }

        SettableFuture<MetricValuesMessage> future = SettableFuture.create();
        future.set(metricValues);
        return Optional.of(future);
    }

}
