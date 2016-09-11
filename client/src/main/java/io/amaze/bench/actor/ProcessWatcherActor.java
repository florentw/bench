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
import io.amaze.bench.api.metric.Metrics;
import oshi.json.SystemInfo;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An actor that monitors specific processes' metrics using polling.<br/>
 * The polling period is customizable, but must be at least one second.<br/>
 * It also allows getting differential metrics using a stopwatch mechanism.<br/>
 * <br/>
 * Using sampling you can get the following metrics about a specific process:
 * <ul>
 * <li><strong>Virtual Memory Size (VSZ):</strong> It includes all memory
 * that the process can access, including memory that is swapped out
 * and memory that is from shared libraries</li>
 * <li><strong>Resident Set Size (RSS):</strong> Returns the Resident Set Size (RSS). It is used to show how much
 * memory is allocated to that process and is in RAM. It does not
 * include memory that is swapped out. It does include memory from
 * shared libraries as long as the pages from those libraries are
 * actually in memory. It does include all stack and heap memory.</li>
 * <li><strong>CPU Kernel time:</strong> Returns the number of milliseconds the process has executed in
 * kernel mode</li>
 * <li><strong>CPU User time:</strong> Returns the number of milliseconds the process has executed in
 * user mode</li>
 * <li><strong>Thread count:</strong> Number of threads in the process</li>
 * </ul>
 *
 * In stopwatch mode, process metrics are read when started and when stopped, a delta of each metric is then produced.
 * The following metrics are then available for a given stopwatch:
 * <ul>
 * <li><strong>Virtual Memory Size (VSZ):</strong> Before, after and delta.</li>
 * <li><strong>Resident Set Size (RSS):</strong> Before, after and delta.</li>
 * <li><strong>CPU Kernel time:</strong> Before, after and delta.</li>
 * <li><strong>CPU User time:</strong> Before, after and delta.</li>
 * <li><strong>Elapsed:</strong> Elapsed time of the stopwatch</li>
 * </ul>
 *
 * @see ProcessWatcherActorInput Actor input message
 */
@Actor
public final class ProcessWatcherActor extends AbstractWatcherActor implements Reactor<ProcessWatcherActorInput> {

    private final Map<ProcessWatcherActorInput, ScheduledFuture> samplerFutures = new HashMap<>();
    private final Map<StopWatchKey, StopwatchThread> stopWatches = new HashMap<>();

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
            case START_STOPWATCH:
                startStopwatch(message);
                break;
            case STOP_STOPWATCH:
                stopStopwatch(message);
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

    private void startStopwatch(final ProcessWatcherActorInput message) throws RecoverableException {
        StopWatchKey stopWatchKey = new StopWatchKey(message.getPid(), message.getMetricKeyPrefix());
        StopwatchThread stopWatchThread = new StopwatchThread(metrics, systemInfo, message);
        synchronized (stopWatches) {
            if (stopWatches.get(stopWatchKey) != null) {
                throw new RecoverableException("Stopwatch already started.");
            }

            stopWatches.put(stopWatchKey, stopWatchThread);
        }
        submit(stopWatchThread);
    }

    private void stopStopwatch(final ProcessWatcherActorInput message) throws RecoverableException {
        StopWatchKey stopWatchKey = new StopWatchKey(message.getPid(), message.getMetricKeyPrefix());
        synchronized (stopWatches) {
            StopwatchThread thread = stopWatches.remove(stopWatchKey);
            if (thread == null) {
                throw new RecoverableException("Stopwatch already stopped.");
            }
            thread.stop();
        }
    }

    private void stopSampling(final ProcessWatcherActorInput message) {
        synchronized (samplerFutures) {
            ScheduledFuture future = samplerFutures.remove(message);
            cancel(future);
        }
    }

    private void cancelTasks() {
        synchronized (samplerFutures) {
            samplerFutures.values().forEach(this::cancel);
            samplerFutures.clear();
        }
    }

    private void resetSampling(final ProcessWatcherActorInput message) {
        synchronized (samplerFutures) {
            ScheduledFuture previousFuture = samplerFutures.remove(message);
            ProcessSamplingThread watcherThread = new ProcessSamplingThread(metrics, systemInfo, message);
            ScheduledFuture future = reschedule(previousFuture, watcherThread, message.getPeriodSeconds());
            samplerFutures.put(message, future);
        }
    }

    static final class StopWatchKey {
        private final int pid;
        private final String metricKeyPrefix;

        StopWatchKey(int pid, String metricKeyPrefix) {
            this.pid = pid;
            this.metricKeyPrefix = metricKeyPrefix;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pid, metricKeyPrefix);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            StopWatchKey that = (StopWatchKey) o;
            return Objects.equals(pid, that.pid) && //
                    Objects.equals(metricKeyPrefix, that.metricKeyPrefix);
        }
    }
}
