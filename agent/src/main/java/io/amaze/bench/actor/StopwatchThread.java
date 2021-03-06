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

import com.google.common.base.Stopwatch;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.json.SystemInfo;
import oshi.json.software.os.OSProcess;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;
import static io.amaze.bench.actor.WatcherActorConstants.UNIT_BYTES;
import static io.amaze.bench.actor.WatcherActorConstants.UNIT_MILLIS;
import static io.amaze.bench.api.metric.Metric.metric;
import static java.lang.String.format;

/**
 * One-time task that will measure the difference between process metrics between calls to startWatch and stopWatch.
 */
final class StopwatchThread implements Runnable {

    private static final Logger log = LogManager.getLogger();

    private final ProcessWatcherActorInput message;
    private final Metrics metrics;
    private final SystemInfo systemInfo;
    private final Stopwatch stopwatch;

    private final CountDownLatch stopping = new CountDownLatch(1);
    private final CountDownLatch stopped = new CountDownLatch(1);

    private long beforeVirtualSize;
    private long beforeResidentSetSize;
    private long beforeKernelTime;
    private long beforeUserTime;

    StopwatchThread(final Metrics metrics, final SystemInfo systemInfo, final ProcessWatcherActorInput message) {
        this.metrics = metrics;
        this.systemInfo = systemInfo;
        this.message = message;

        stopwatch = Stopwatch.createUnstarted();
    }

    @Override
    public void run() {
        try {
            Optional<OSProcess> beforeProcess = process();
            if (!beforeProcess.isPresent()) {
                return;
            }

            startMetrics(beforeProcess.get());

            awaitUninterruptibly(stopping);

            Optional<OSProcess> afterProcess = process();
            if (!afterProcess.isPresent()) {
                return;
            }

            stopMetrics(afterProcess.get());
        } finally {
            stopped.countDown();
        }
    }

    void stop() {
        stopping.countDown();
        awaitUninterruptibly(stopped);
    }

    private Optional<OSProcess> process() {
        try {
            return Optional.ofNullable(systemInfo.getOperatingSystem().getProcess(message.getPid()));
        } catch (Exception e) { // NOSONAR - We want to catch everything
            log.warn("Pid {} not found", message.getPid(), e);
            return Optional.empty();
        }
    }

    private void stopMetrics(final OSProcess afterProcess) {
        long afterVirtualSize = afterProcess.getVirtualSize();
        long afterResidentSetSize = afterProcess.getResidentSetSize();
        long afterKernelTime = afterProcess.getKernelTime();
        long afterUserTime = afterProcess.getUserTime();

        long now = System.currentTimeMillis();
        metrics.sinkFor(afterVirtualSize()).timed(now, afterVirtualSize);
        metrics.sinkFor(deltaVirtualSize()).add(afterVirtualSize - beforeVirtualSize);

        metrics.sinkFor(afterResidentSet()).timed(now, afterResidentSetSize);
        metrics.sinkFor(deltaResidentSet()).add(afterResidentSetSize - beforeResidentSetSize);

        metrics.sinkFor(afterKernelTime()).timed(now, afterKernelTime);
        metrics.sinkFor(deltaKernelTime()).add(afterKernelTime - beforeKernelTime);

        metrics.sinkFor(afterUserTime()).timed(now, afterUserTime);
        metrics.sinkFor(deltaUserTime()).add(afterUserTime - beforeUserTime);

        metrics.sinkFor(elapsed()).add(stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    private void startMetrics(final OSProcess beforeProcess) {
        stopwatch.start();

        beforeVirtualSize = beforeProcess.getVirtualSize();
        beforeResidentSetSize = beforeProcess.getResidentSetSize();
        beforeKernelTime = beforeProcess.getKernelTime();
        beforeUserTime = beforeProcess.getUserTime();

        long now = System.currentTimeMillis();
        metrics.sinkFor(beforeVirtualSize()).timed(now, beforeVirtualSize);
        metrics.sinkFor(beforeResidentSet()).timed(now, beforeResidentSetSize);
        metrics.sinkFor(beforeKernelTime()).timed(now, beforeKernelTime);
        metrics.sinkFor(beforeUserTime()).timed(now, beforeUserTime);
    }

    private Metric beforeVirtualSize() {
        return metric(format("proc.%s.before.mem.virtualSize", message.getMetricKeyPrefix()), UNIT_BYTES) //
                .label(format("Virtual memory usage before %s", message.getMetricLabelSuffix())).minValue(0).build();
    }

    private Metric afterVirtualSize() {
        return metric(format("proc.%s.after.mem.virtualSize", message.getMetricKeyPrefix()), UNIT_BYTES) //
                .label(format("Virtual memory usage after %s", message.getMetricLabelSuffix())).minValue(0).build();
    }

    private Metric deltaVirtualSize() {
        return metric(format("proc.%s.delta.mem.virtualSize", message.getMetricKeyPrefix()), UNIT_BYTES) //
                .label(format("Virtual memory usage delta %s", message.getMetricLabelSuffix())).minValue(0).build();
    }

    private Metric beforeResidentSet() {
        return metric(format("proc.%s.before.mem.residentRet", message.getMetricKeyPrefix()), UNIT_BYTES) //
                .label(format("RAM usage before %s", message.getMetricLabelSuffix())).minValue(0).build();
    }

    private Metric afterResidentSet() {
        return metric(format("proc.%s.after.mem.residentRet", message.getMetricKeyPrefix()), UNIT_BYTES) //
                .label(format("RAM usage after %s", message.getMetricLabelSuffix())).minValue(0).build();
    }

    private Metric deltaResidentSet() {
        return metric(format("proc.%s.delta.mem.residentRet", message.getMetricKeyPrefix()), UNIT_BYTES) //
                .label(format("RAM usage delta %s", message.getMetricLabelSuffix())).minValue(0).build();
    }

    private Metric beforeKernelTime() {
        return metric(format("proc.%s.before.cpu.kernelTime", message.getMetricKeyPrefix()), UNIT_MILLIS) //
                .label(format("CPU sys time before %s", message.getMetricLabelSuffix())).minValue(0).build();
    }

    private Metric afterKernelTime() {
        return metric(format("proc.%s.after.cpu.kernelTime", message.getMetricKeyPrefix()), UNIT_MILLIS) //
                .label(format("CPU sys time after %s", message.getMetricLabelSuffix())).minValue(0).build();
    }

    private Metric deltaKernelTime() {
        return metric(format("proc.%s.delta.cpu.kernelTime", message.getMetricKeyPrefix()), UNIT_MILLIS) //
                .label(format("CPU sys time delta %s", message.getMetricLabelSuffix())).minValue(0).build();
    }

    private Metric beforeUserTime() {
        return metric(format("proc.%s.before.cpu.userTime", message.getMetricKeyPrefix()), UNIT_MILLIS) //
                .label(format("CPU user time before %s", message.getMetricLabelSuffix())).minValue(0).build();
    }

    private Metric afterUserTime() {
        return metric(format("proc.%s.after.cpu.userTime", message.getMetricKeyPrefix()), UNIT_MILLIS) //
                .label(format("CPU user time after %s", message.getMetricLabelSuffix())).minValue(0).build();
    }

    private Metric deltaUserTime() {
        return metric(format("proc.%s.delta.cpu.userTime", message.getMetricKeyPrefix()), UNIT_MILLIS) //
                .label(format("CPU user time delta %s", message.getMetricLabelSuffix())).minValue(0).build();
    }

    private Metric elapsed() {
        return metric(format("proc.%s.elapsed", message.getMetricKeyPrefix()), UNIT_MILLIS) //
                .label(format("Elapsed time %s", message.getMetricLabelSuffix())).minValue(0).build();
    }
}
