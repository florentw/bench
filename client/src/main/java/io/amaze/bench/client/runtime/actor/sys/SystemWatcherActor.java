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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.Uninterruptibles;
import io.amaze.bench.api.*;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import oshi.json.SystemInfo;

import javax.validation.constraints.NotNull;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.amaze.bench.api.metric.Metric.metric;

/**
 * An actor that monitors the system status using polling.<br/>
 * The polling period is customizable, but must be at least one second.
 *
 * It will produce the following metrics:
 * <ul>
 * <li>{@link #METRIC_LOAD_AVERAGE} the current load average if available.</li>
 * <li>{@link #METRIC_CPU_USAGE} the current CPU usage in percent if available.</li>
 * <li>{@link #METRIC_SWAP_USED} the current use of swap memory if available.</li>
 * <li>{@link #METRIC_AVAILABLE_RAM} the current amount of RAM used if available.</li>
 * </ul>
 */
@Actor
public final class SystemWatcherActor implements Reactor<SystemWatcherInput> {

    static final Metric METRIC_LOAD_AVERAGE = metric("sys.LoadAverage", "none").label("Load Average").build();
    static final Metric METRIC_CPU_USAGE = metric("sys.cpu.Usage", "%").label("CPU Usage").build();
    static final Metric METRIC_SWAP_USED = metric("sys.mem.SwapUsed", "bytes").label("Swap used").build();
    static final Metric METRIC_AVAILABLE_RAM = metric("sys.mem.AvailableMemory",
                                                      "bytes").label("Available RAM").build();

    private final ScheduledExecutorService scheduler;
    private final SystemWatcherThread watcherThread;

    private volatile ScheduledFuture<?> scheduledTask;

    public SystemWatcherActor(final Metrics metrics) {
        this(metrics, initScheduler());
    }

    @VisibleForTesting
    SystemWatcherActor(final Metrics metrics, final ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
        SystemInfo systemInfo = new SystemInfo();
        watcherThread = new SystemWatcherThread(systemInfo, checkNotNull(metrics));
    }

    private static ScheduledExecutorService initScheduler() {
        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setDaemon(true);
        builder.setNameFormat("SystemWatcher-%d");
        return Executors.newScheduledThreadPool(1, builder.build());
    }

    @Override
    public void onMessage(@NotNull final String from, @NotNull final SystemWatcherInput message)
            throws ReactorException {
        checkNotNull(from);
        checkNotNull(message);

        switch (message.getCommand()) {
            case START:
            case SET_PERIOD:
                rescheduleTask(message);
                break;
            case STOP:
                cancelTask();
                break;
            default:
                throw new IrrecoverableException("Unsupported operation.");
        }
    }

    @After
    public void closeThreads() {
        cancelTask();
        scheduler.shutdownNow();
    }

    private synchronized void cancelTask() {
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
            try {
                Uninterruptibles.getUninterruptibly(scheduledTask);
            } catch (CancellationException | ExecutionException ignored) { // NOSONAR
            }
        }
    }

    private synchronized void rescheduleTask(final SystemWatcherInput message) {
        cancelTask();
        scheduledTask = scheduler.scheduleAtFixedRate(watcherThread, 0, message.getIntervalSeconds(), TimeUnit.SECONDS);
    }

    private static final class SystemWatcherThread implements Runnable {

        private final SystemInfo systemInfo;

        private final Metrics.Sink loadAverageSink;
        private final Metrics.Sink systemCpuLoadSink;
        private final Metrics.Sink availableRamSink;
        private final Metrics.Sink swapUsedSink;

        private SystemWatcherThread(final SystemInfo systemInfo, final Metrics metrics) {
            this.systemInfo = systemInfo;

            loadAverageSink = metrics.sinkFor(METRIC_LOAD_AVERAGE);
            systemCpuLoadSink = metrics.sinkFor(METRIC_CPU_USAGE);
            availableRamSink = metrics.sinkFor(METRIC_AVAILABLE_RAM);
            swapUsedSink = metrics.sinkFor(METRIC_SWAP_USED);
        }

        @Override
        public void run() {
            double loadAverage = systemInfo.getHardware().getProcessor().getSystemLoadAverage();
            double systemCpuLoad = systemInfo.getHardware().getProcessor().getSystemCpuLoad();
            long availableRam = systemInfo.getHardware().getMemory().getAvailable();
            long swapUsed = systemInfo.getHardware().getMemory().getSwapUsed();

            long now = System.currentTimeMillis();
            produceIfValid(loadAverage, now, loadAverageSink);
            produceIfValid(systemCpuLoad, now, systemCpuLoadSink);
            produceIfValid(availableRam, now, availableRamSink);
            produceIfValid(swapUsed, now, swapUsedSink);
        }

        private void produceIfValid(final long value, final long now, final Metrics.Sink sink) {
            if (value >= 0) {
                sink.timed(now, value);
            }
        }

        private void produceIfValid(final double value, final long now, final Metrics.Sink sink) {
            if (value >= 0) {
                sink.timed(now, value);
            }
        }
    }

}
