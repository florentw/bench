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
package io.amaze.bench.client.runtime.actor.sys;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 9/4/16.
 */
public final class SystemWatcherInput implements Serializable {

    private final Command command;
    private final long intervalSeconds;

    public SystemWatcherInput(final Command command, final long intervalSeconds) {
        checkArgument(intervalSeconds > 0, "Interval can't be less than 1 second, was " + intervalSeconds);

        this.command = checkNotNull(command);
        this.intervalSeconds = intervalSeconds;
    }

    public Command getCommand() {
        return command;
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public enum Command {
        SET_PERIOD, START, STOP
    }

}
