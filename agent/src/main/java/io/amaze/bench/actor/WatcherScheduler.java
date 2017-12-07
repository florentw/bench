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

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.Closeable;
import java.util.concurrent.*;

import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;
import static java.util.Objects.requireNonNull;

/**
 * Scheduler to be used by monitoring actors, encapsulates interactions with the {@link ScheduledExecutorService}.
 */
public final class WatcherScheduler implements Closeable {

    private final ScheduledExecutorService scheduler;

    public WatcherScheduler(final String schedulerThreadNameFormat) {
        this(initScheduler(requireNonNull(schedulerThreadNameFormat)));
    }

    WatcherScheduler(final ScheduledExecutorService scheduler) {
        this.scheduler = requireNonNull(scheduler);
    }

    public synchronized void submit(final Runnable taskToSubmit) {
        scheduler.schedule(taskToSubmit, 0, TimeUnit.SECONDS);
    }

    public synchronized void cancel(final ScheduledFuture<?> scheduledTask) {
        scheduledTask.cancel(true);
        try {
            getUninterruptibly(scheduledTask);
        } catch (CancellationException | ExecutionException ignored) { // NOSONAR
        }
    }

    public synchronized ScheduledFuture reschedule(final ScheduledFuture previousFuture,
                                                   final Runnable taskToSchedule,
                                                   final long periodSeconds) {
        if (previousFuture != null) {
            cancel(previousFuture);
        }
        return scheduler.scheduleAtFixedRate(taskToSchedule, 0, periodSeconds, TimeUnit.SECONDS);
    }

    @Override
    public final synchronized void close() {
        scheduler.shutdownNow();
    }

    private static ScheduledExecutorService initScheduler(final String nameFormat) {
        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setDaemon(true);
        builder.setNameFormat(nameFormat);
        return Executors.newScheduledThreadPool(1, builder.build());
    }
}
