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

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;
import static io.amaze.bench.actor.WatcherActorConstants.MSG_PERIOD_LESS_THAN_ONE_SEC;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Input message for {@link SystemWatcherActor}. Static methods are provided to create the messages.
 *
 * @see SystemWatcherActor
 */
public final class SystemWatcherInput implements Serializable {

    private static final int NA = 1;
    private final Command command;
    private final long periodSeconds;

    private SystemWatcherInput(final Command command, final long periodSeconds) {
        checkArgument(periodSeconds > 0, format(MSG_PERIOD_LESS_THAN_ONE_SEC, periodSeconds));

        this.command = requireNonNull(command);
        this.periodSeconds = periodSeconds;
    }

    public static SystemWatcherInput start(final long periodSeconds) {
        return new SystemWatcherInput(Command.START, periodSeconds);
    }

    public static SystemWatcherInput setPeriod(final long periodSeconds) {
        return new SystemWatcherInput(Command.SET_PERIOD, periodSeconds);
    }

    public static SystemWatcherInput stop() {
        return new SystemWatcherInput(Command.STOP, NA);
    }

    long getPeriodSeconds() {
        return periodSeconds;
    }

    Command getCommand() {
        return command;
    }

    enum Command {
        SET_PERIOD, START, STOP
    }

}
