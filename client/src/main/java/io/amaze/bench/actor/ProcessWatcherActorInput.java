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
package io.amaze.bench.actor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.amaze.bench.actor.AbstractWatcherActor.MSG_PERIOD_LESS_THAN_ONE_SEC;
import static io.amaze.bench.actor.ProcessWatcherActorInput.Command.*;
import static java.lang.String.format;

/**
 * Input message for {@link ProcessWatcherActor}. Static methods are provided to create the messages.
 *
 * @see ProcessWatcherActor
 */
public final class ProcessWatcherActorInput implements Serializable {

    private static final String NA = "n/a";
    private static final int NA_PERIOD = 1;

    private final Command command;
    private final int pid;
    private final long periodSeconds;
    private final String metricKeyPrefix;
    private final String metricLabelSuffix;

    private ProcessWatcherActorInput(final Command command,
                                     final int pid,
                                     final long periodSeconds,
                                     final String metricKeyPrefix, final String metricLabelSuffix) {

        checkArgument(!metricKeyPrefix.trim().isEmpty(), "Key cannot be empty.");
        checkArgument(pid > 0, format("Invalid PID %d", pid));
        checkArgument(periodSeconds > 0, format(MSG_PERIOD_LESS_THAN_ONE_SEC, periodSeconds));

        this.command = checkNotNull(command);
        this.pid = pid;
        this.periodSeconds = periodSeconds;

        this.metricKeyPrefix = checkNotNull(metricKeyPrefix);
        this.metricLabelSuffix = checkNotNull(metricLabelSuffix);
    }

    public static ProcessWatcherActorInput startSampling(final int pid,
                                                         final long periodSeconds,
                                                         @NotNull final String metricKeyPrefix) {
        return new ProcessWatcherActorInput(START_SAMPLING,
                                            pid,
                                            periodSeconds,
                                            metricKeyPrefix,
                                            defaultLabelSuffix(pid));
    }

    public static ProcessWatcherActorInput startSampling(final int pid,
                                                         final long periodSeconds,
                                                         @NotNull final String metricKeyPrefix,
                                                         @NotNull final String metricLabelSuffix) {
        return new ProcessWatcherActorInput(START_SAMPLING, pid, periodSeconds, metricKeyPrefix, metricLabelSuffix);
    }

    public static ProcessWatcherActorInput stopSampling(final int pid) {
        return new ProcessWatcherActorInput(STOP_SAMPLING, pid, NA_PERIOD, NA, NA);
    }

    public static ProcessWatcherActorInput startStopwatch(int pid,
                                                          @NotNull final String metricKeyPrefix,
                                                          @NotNull final String metricLabelSuffix) {
        return new ProcessWatcherActorInput(START_STOPWATCH, pid, NA_PERIOD, metricKeyPrefix, metricLabelSuffix);
    }

    public static ProcessWatcherActorInput startStopwatch(final int pid, @NotNull final String metricKeyPrefix) {
        return new ProcessWatcherActorInput(START_STOPWATCH, pid, NA_PERIOD, metricKeyPrefix, defaultLabelSuffix(pid));
    }

    public static ProcessWatcherActorInput stopStopwatch(final int pid, @NotNull final String keyPrefix) {
        return new ProcessWatcherActorInput(STOP_STOPWATCH, pid, NA_PERIOD, keyPrefix, NA);
    }

    private static String defaultLabelSuffix(final int pid) {
        return "pid:" + pid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProcessWatcherActorInput that = (ProcessWatcherActorInput) o;
        return pid == that.pid;
    }

    Command getCommand() {
        return command;
    }

    int getPid() {
        return pid;
    }

    long getPeriodSeconds() {
        return periodSeconds;
    }

    String getMetricKeyPrefix() {
        return metricKeyPrefix;
    }

    String getMetricLabelSuffix() {
        return metricLabelSuffix;
    }

    enum Command {
        START_SAMPLING, STOP_SAMPLING, //
        START_STOPWATCH, STOP_STOPWATCH
    }
}
