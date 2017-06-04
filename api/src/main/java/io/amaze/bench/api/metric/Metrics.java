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
package io.amaze.bench.api.metric;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Provides the ability for an actor to produce metric values to be collected and centralized asynchronously.
 *
 * @see Metric Represent a metric for which multiple {@link Number} values can be produced.
 * @see Metrics Provides a way to create a {@link Sink} instance.
 */
@FunctionalInterface
public interface Metrics extends Serializable {

    /**
     * @param metric Metric for which to get a {@link Sink} instance for.
     * @return A sink instance of this particular {@link Metric}.
     */
    Sink sinkFor(@NotNull Metric metric);

    /**
     * Offers a facade to produce metrics values.<br>
     * Metrics produced through the {@link Sink} instance are then collected and centralized.
     */
    interface Sink {

        /**
         * Add an arbitrary {@link Number} value to this sink.
         *
         * @param value Value to be added
         * @return This instance for chaining calls.
         */
        Sink add(@NotNull Number value);

        /**
         * Add an arbitrary timed value to this sink.
         *
         * @param timeStamp Java timestamp
         * @param value     Value to be added
         * @return This instance for chaining calls.
         */
        Sink timed(@NotNull long timeStamp, @NotNull Number value);

        /**
         * Add an arbitrary {@link Number} value to this sink.<br>
         * The current timestamp will be assigned behind the scenes.
         *
         * @param value Value to be added
         * @return This instance for chaining calls.
         */
        Sink timed(@NotNull Number value);
    }
}
