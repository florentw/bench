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

import io.amaze.bench.api.metric.Metrics;
import oshi.json.SystemInfo;
import oshi.json.hardware.HardwareAbstractionLayer;

/**
 * Created on 10/30/16.
 */
final class SystemWatcherThread implements Runnable {

    private final SystemInfo systemInfo;

    private final Metrics.Sink loadAverageSink;
    private final Metrics.Sink systemCpuLoadSink;
    private final Metrics.Sink availableRamSink;
    private final Metrics.Sink swapUsedSink;

    SystemWatcherThread(final SystemInfo systemInfo, final Metrics metrics) {
        this.systemInfo = systemInfo;

        loadAverageSink = metrics.sinkFor(SystemWatcherActor.METRIC_LOAD_AVERAGE);
        systemCpuLoadSink = metrics.sinkFor(SystemWatcherActor.METRIC_CPU_USAGE);
        availableRamSink = metrics.sinkFor(SystemWatcherActor.METRIC_AVAILABLE_RAM);
        swapUsedSink = metrics.sinkFor(SystemWatcherActor.METRIC_SWAP_USED);
    }

    @Override
    public void run() {
        HardwareAbstractionLayer hardware = systemInfo.getHardware();

        double loadAverage = hardware.getProcessor().getSystemLoadAverage();
        double systemCpuLoad = hardware.getProcessor().getSystemCpuLoad();
        long availableRam = hardware.getMemory().getAvailable();
        long swapUsed = hardware.getMemory().getSwapUsed();

        long now = System.currentTimeMillis();
        produceIfValid(loadAverage, now, loadAverageSink);
        produceIfValid(systemCpuLoad, now, systemCpuLoadSink);
        produceIfValid(availableRam, now, availableRamSink);
        produceIfValid(swapUsed, now, swapUsedSink);
    }

    private static <T extends Number> void produceIfValid(final T value, //
                                                          final long now, //
                                                          final Metrics.Sink sink) {
        if (value.doubleValue() >= 0) {
            sink.timed(now, value);
        }
    }
}
