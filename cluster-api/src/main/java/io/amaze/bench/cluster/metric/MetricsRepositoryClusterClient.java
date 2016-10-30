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
package io.amaze.bench.cluster.metric;

import javax.validation.constraints.NotNull;
import java.io.Closeable;

/**
 * Facade to add a listener on metrics produced by actors.
 *
 * @see MetricsRepositoryListener
 */
public interface MetricsRepositoryClusterClient extends Closeable {

    /**
     * Register the given listeners to be plugged to the underlying message system.
     * {@link MetricsRepositoryListener} instance will be notified of metrics related events.
     *
     * @param metricsListener Listener that will be called upon new metrics notifications.
     */
    void startMetricsListener(@NotNull MetricsRepositoryListener metricsListener);

    @Override
    void close();

}
