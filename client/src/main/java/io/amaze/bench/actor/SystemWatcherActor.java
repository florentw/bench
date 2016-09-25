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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.json.SystemInfo;

import javax.validation.constraints.NotNull;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.amaze.bench.api.metric.Metric.metric;

/**
 * An actor that monitors the system status using polling.<br>
 * The polling period is customizable, but must be at least one second.
 *
 * It will produce the following metrics:
 * <ul>
 * <li>{@link #METRIC_LOAD_AVERAGE} the current load average if available.</li>
 * <li>{@link #METRIC_CPU_USAGE} the current CPU usage in percent if available.</li>
 * <li>{@link #METRIC_SWAP_USED} the current use of swap memory if available.</li>
 * <li>{@link #METRIC_AVAILABLE_RAM} the current amount of RAM used if available.</li>
 * </ul>
 *
 * @see SystemWatcherInput Actor input message
 */
@Actor
public final class SystemWatcherActor extends AbstractWatcherActor implements Reactor<SystemWatcherInput> {

    public static final Metric METRIC_LOAD_AVERAGE = metric("sys.loadAverage", "none") //
            .label("Load Average").build();
    public static final Metric METRIC_CPU_USAGE = metric("sys.cpu.usage", "%") //
            .label("CPU Usage").minValue(0d).maxValue(1d).build();
    public static final Metric METRIC_SWAP_USED = metric("sys.mem.swapUsed", UNIT_BYTES) //
            .label("Swap used").build();
    public static final Metric METRIC_AVAILABLE_RAM = metric("sys.mem.availableMemory", UNIT_BYTES) //
            .label("Available RAM").build();

    private static final Logger LOG = LogManager.getLogger(SystemWatcherActor.class);

    private final SystemWatcherThread watcherThread;

    private volatile ScheduledFuture<?> currentTaskHandle;

    public SystemWatcherActor(final Metrics metrics) {
        super("SystemWatcher-%d");
        watcherThread = watcherThread(metrics);
    }

    @VisibleForTesting
    SystemWatcherActor(final Metrics metrics, final ScheduledExecutorService scheduler) {
        super(scheduler);
        watcherThread = watcherThread(metrics);
    }

    @Override
    public void onMessage(@NotNull final String from, @NotNull final SystemWatcherInput message)
            throws ReactorException {
        checkNotNull(from);
        checkNotNull(message);

        switch (message.getCommand()) {
            case START:
            case SET_PERIOD:
                LOG.info("System monitoring rescheduled every {} second(s)", message.getPeriodSeconds());
                currentTaskHandle = reschedule(currentTaskHandle, watcherThread, message.getPeriodSeconds());
                break;
            case STOP:
                LOG.info("System monitoring stopped.");
                cancelTask();
                break;
            default:
                throw new IrrecoverableException(MSG_UNSUPPORTED_COMMAND);
        }
    }

    @After
    public void closeThreads() {
        cancelTask();
        closeScheduler();
    }

    private SystemWatcherThread watcherThread(final Metrics metrics) {
        SystemInfo systemInfo = new SystemInfo();
        return new SystemWatcherThread(systemInfo, checkNotNull(metrics));
    }

    private void cancelTask() {
        if (currentTaskHandle != null) {
            cancel(currentTaskHandle);
        }
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

        private <T extends Number> void produceIfValid(final T value, final long now, final Metrics.Sink sink) {
            if (value.doubleValue() >= 0) {
                sink.timed(now, value);
            }
        }
    }

}
