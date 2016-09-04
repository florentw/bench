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
package io.amaze.bench.api;


import io.amaze.bench.shared.metric.Metric;

import javax.validation.constraints.NotNull;

/**
 * Interface that can be injected in an Actor's constructor.<br/>
 * It allows the actor to produce metrics, that will be sent to the master for collection.
 */
@FunctionalInterface
public interface MetricsCollector {

    void put(@NotNull String key, @NotNull Metric metric);

}
