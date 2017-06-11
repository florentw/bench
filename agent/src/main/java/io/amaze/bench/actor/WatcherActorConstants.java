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

package io.amaze.bench.actor;

/**
 * Created on 6/11/17.
 */
final class WatcherActorConstants {

    static final String UNIT_BYTES = "bytes";
    static final String UNIT_MILLIS = "ms";
    static final String MSG_UNSUPPORTED_COMMAND = "Unsupported command.";
    static final String MSG_PERIOD_LESS_THAN_ONE_SEC = "Period can't be less than 1 second, was %d.";

    private WatcherActorConstants() {
        // Constants, should not be instantiated
    }
}
