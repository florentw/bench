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
package io.amaze.bench.runtime.cluster.registry;

import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.actor.metric.MetricValuesMessage;

import javax.validation.constraints.NotNull;

/**
 * Created on 10/3/16.
 */
@FunctionalInterface
public interface MetricsRepositoryListener {

    void onMetricValues(@NotNull ActorKey actor, @NotNull MetricValuesMessage metrics);

}
