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

import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.amaze.bench.actor.AbstractWatcherActor.MSG_PERIOD_LESS_THAN_ONE_SEC;
import static io.amaze.bench.actor.ProcessWatcherActorInput.Command.START_SAMPLING;
import static io.amaze.bench.actor.ProcessWatcherActorInput.Command.STOP_SAMPLING;
import static java.lang.String.format;

/**
 * Created on 9/10/16.
 */
public final class ProcessWatcherActorInput implements Serializable {

    private static final String NA = "n/a";

    private final Command command;
    private final int pid;
    private final long periodSeconds;
    private final String metricKeyPrefix;
    private final String metricLabel;

    private ProcessWatcherActorInput(final Command command,
                                     final int pid,
                                     final long periodSeconds,
                                     final String metricKeyPrefix,
                                     final String metricLabel) {

        checkArgument(!metricKeyPrefix.trim().isEmpty(), "Key cannot be empty.");
        checkArgument(pid > 0, format("Invalid PID %d", pid));
        checkArgument(periodSeconds > 0, format(MSG_PERIOD_LESS_THAN_ONE_SEC, periodSeconds));

        this.command = checkNotNull(command);
        this.pid = pid;
        this.periodSeconds = periodSeconds;

        this.metricKeyPrefix = checkNotNull(metricKeyPrefix);
        this.metricLabel = checkNotNull(metricLabel);
    }

    public static ProcessWatcherActorInput startSampling(int pid, long periodSeconds, String metricKeyPrefix) {
        return new ProcessWatcherActorInput(START_SAMPLING, pid, periodSeconds, metricKeyPrefix, "pid:" + pid);
    }

    public static ProcessWatcherActorInput startSampling(int pid,
                                                         long periodSeconds,
                                                         String metricKeyPrefix,
                                                         String metricLabel) {
        return new ProcessWatcherActorInput(START_SAMPLING, pid, periodSeconds, metricKeyPrefix, metricLabel);
    }

    public static ProcessWatcherActorInput stopSampling(int pid) {
        return new ProcessWatcherActorInput(STOP_SAMPLING, pid, 1, NA, NA);
    }

    public Command getCommand() {
        return command;
    }

    public int getPid() {
        return pid;
    }

    public long getPeriodSeconds() {
        return periodSeconds;
    }

    public String getMetricKeyPrefix() {
        return metricKeyPrefix;
    }

    public String getMetricLabel() {
        return metricLabel;
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

    enum Command {
        START_SAMPLING, STOP_SAMPLING
    }
}
