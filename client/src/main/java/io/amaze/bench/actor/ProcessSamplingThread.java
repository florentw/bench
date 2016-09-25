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

import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.json.SystemInfo;
import oshi.json.software.os.OSProcess;

import static io.amaze.bench.api.metric.Metric.metric;
import static java.lang.String.format;

/**
 * Task that will be periodically executed to watch process-specific metrics such as CPU, memory, etc.
 */
final class ProcessSamplingThread implements Runnable {

    private static final Logger LOG = LogManager.getLogger(ProcessSamplingThread.class);

    private final SystemInfo systemInfo;
    private final ProcessWatcherActorInput message;

    private final Metrics.Sink virtualSizeSink;
    private final Metrics.Sink residentSetSink;
    private final Metrics.Sink kernelTimeSink;
    private final Metrics.Sink userTimeSink;
    private final Metrics.Sink threadCountSink;

    ProcessSamplingThread(final Metrics metrics, final SystemInfo systemInfo, final ProcessWatcherActorInput message) {
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
        OSProcess process = processOrNull();
        if (process == null) {
            return;
        }

        long now = System.currentTimeMillis();
        virtualSizeSink.timed(now, process.getVirtualSize());
        residentSetSink.timed(now, process.getResidentSetSize());
        kernelTimeSink.timed(now, process.getKernelTime());
        userTimeSink.timed(now, process.getUserTime());
        threadCountSink.timed(now, process.getThreadCount());
    }

    private OSProcess processOrNull() {
        try {
            return systemInfo.getOperatingSystem().getProcess(message.getPid());
        } catch (Exception e) {
            LOG.warn("Pid not found {}", message.getPid(), e);
            return null;
        }
    }

    private Metric virtualSize(final ProcessWatcherActorInput message) {
        return metric(format("proc.%s.mem.virtualSize", message.getMetricKeyPrefix()),
                      AbstractWatcherActor.UNIT_BYTES) //
                .label("Virtual memory usage " + message.getMetricLabelSuffix()).minValue(0).build();
    }

    private Metric residentSet(final ProcessWatcherActorInput message) {
        return metric(format("proc.%s.mem.residentRet", message.getMetricKeyPrefix()),
                      AbstractWatcherActor.UNIT_BYTES) //
                .label(format("RAM usage %s", message.getMetricLabelSuffix())).minValue(0).build();
    }

    private Metric kernelTime(final ProcessWatcherActorInput message) {
        return metric(format("proc.%s.cpu.kernelTime", message.getMetricKeyPrefix()),
                      AbstractWatcherActor.UNIT_MILLIS) //
                .label(format("CPU sys time %s", message.getMetricLabelSuffix())).minValue(0).build();
    }

    private Metric userTime(final ProcessWatcherActorInput message) {
        return metric(format("proc.%s.cpu.userTime", message.getMetricKeyPrefix()), AbstractWatcherActor.UNIT_MILLIS) //
                .label(format("CPU user time %s", message.getMetricLabelSuffix())).minValue(0).build();
    }

    private Metric threadCount(final ProcessWatcherActorInput message) {
        return metric(format("proc.%s.threadCount", message.getMetricKeyPrefix()), "threads") //
                .label("Thread count " + message.getMetricLabelSuffix()).minValue(0).build();
    }
}
