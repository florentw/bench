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

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.api.*;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import oshi.json.SystemInfo;
import oshi.json.software.os.OSProcess;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.amaze.bench.api.metric.Metric.metric;
import static java.lang.String.format;

/**
 * Created on 9/10/16.
 */
@Actor
public final class ProcessWatcherActor extends AbstractWatcherActor implements Reactor<ProcessWatcherActorInput> {

    private final Map<ProcessWatcherActorInput, ScheduledFuture> samplingProcesses = new HashMap<>();
    private final SystemInfo systemInfo = new SystemInfo();
    private final Metrics metrics;

    public ProcessWatcherActor(final Metrics metrics) {
        super(initScheduler("ProcessWatcher-%d"));
        this.metrics = checkNotNull(metrics);
    }

    @VisibleForTesting
    ProcessWatcherActor(final Metrics metrics, final ScheduledExecutorService scheduler) {
        super(scheduler);
        this.metrics = checkNotNull(metrics);
    }

    @Override
    public void onMessage(@NotNull final String from, @NotNull final ProcessWatcherActorInput message)
            throws ReactorException {
        checkNotNull(from);
        checkNotNull(message);

        switch (message.getCommand()) {
            case START_SAMPLING:
                resetSampling(message);
                break;
            case STOP_SAMPLING:
                stopSampling(message);
                break;
            default:
                throw new IrrecoverableException(MSG_UNSUPPORTED_COMMAND);
        }
    }

    @After
    public void closeThreads() {
        cancelTasks();
        closeScheduler();
    }

    private void stopSampling(final ProcessWatcherActorInput message) {
        synchronized (samplingProcesses) {
            ScheduledFuture future = samplingProcesses.remove(message);
            cancel(future);
        }
    }

    private void cancelTasks() {
        synchronized (samplingProcesses) {
            samplingProcesses.values().forEach(this::cancel);
            samplingProcesses.clear();
        }
    }

    private void resetSampling(final ProcessWatcherActorInput message) {
        synchronized (samplingProcesses) {
            ScheduledFuture previousFuture = samplingProcesses.remove(message);
            ProcessWatcherThread watcherThread = new ProcessWatcherThread(metrics, systemInfo, message);
            ScheduledFuture future = reschedule(previousFuture, watcherThread, message.getPeriodSeconds());
            samplingProcesses.put(message, future);
        }
    }

    private static final class ProcessWatcherThread implements Runnable {
        private final SystemInfo systemInfo;
        private final ProcessWatcherActorInput message;

        private final Metrics.Sink virtualSizeSink;
        private final Metrics.Sink residentSetSink;
        private final Metrics.Sink kernelTimeSink;
        private final Metrics.Sink userTimeSink;
        private final Metrics.Sink threadCountSink;

        private ProcessWatcherThread(final Metrics metrics,
                                     final SystemInfo systemInfo,
                                     final ProcessWatcherActorInput message) {
            this.systemInfo = systemInfo;
            this.message = message;

            virtualSizeSink = metrics.sinkFor(virtualSize(message));
            residentSetSink = metrics.sinkFor(residentSet(message));
            kernelTimeSink = metrics.sinkFor(kernelTime(message));
            userTimeSink = metrics.sinkFor(userTime(message));
            threadCountSink = metrics.sinkFor(threadCount(message));
        }

        @Override
        public void run() {
            OSProcess process = systemInfo.getOperatingSystem().getProcess(message.getPid());

            long now = System.currentTimeMillis();
            virtualSizeSink.timed(now, process.getVirtualSize());
            residentSetSink.timed(now, process.getResidentSetSize());
            kernelTimeSink.timed(now, process.getKernelTime());
            userTimeSink.timed(now, process.getUserTime());
            threadCountSink.timed(now, process.getThreadCount());
        }

        private Metric virtualSize(final ProcessWatcherActorInput message) {
            return metric(format("proc.%s.mem.virtualSize", message.getMetricKeyPrefix()), UNIT_BYTES) //
                    .label("Virtual memory usage " + message.getMetricLabel()).minValue(0).build();
        }

        private Metric residentSet(final ProcessWatcherActorInput message) {
            return metric(format("proc.%s.mem.residentRet", message.getMetricKeyPrefix()), UNIT_BYTES) //
                    .label(format("RAM usage %s", message.getMetricLabel())).minValue(0).build();
        }

        private Metric kernelTime(final ProcessWatcherActorInput message) {
            return metric(format("proc.%s.cpu.kernelTime", message.getMetricKeyPrefix()), UNIT_MILLIS) //
                    .label(format("CPU sys time %s", message.getMetricLabel())).minValue(0).build();
        }

        private Metric userTime(final ProcessWatcherActorInput message) {
            return metric(format("proc.%s.cpu.userTime", message.getMetricKeyPrefix()), UNIT_MILLIS) //
                    .label(format("CPU user time %s", message.getMetricLabel())).minValue(0).build();
        }

        private Metric threadCount(final ProcessWatcherActorInput message) {
            return metric(format("proc.%s.thread.count", message.getMetricKeyPrefix()), "threads") //
                    .label("Thread count " + message.getMetricLabel()).minValue(0).build();
        }
    }
}
