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

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;

/**
 * Base class for watcher actors, contains interactions with the {@link ScheduledExecutorService}.
 */
abstract class AbstractWatcherActor {

    static final String UNIT_BYTES = "bytes";
    static final String UNIT_MILLIS = "ms";
    static final String MSG_UNSUPPORTED_COMMAND = "Unsupported command.";
    static final String MSG_PERIOD_LESS_THAN_ONE_SEC = "Period can't be less than 1 second, was %d.";

    private final ScheduledExecutorService scheduler;

    AbstractWatcherActor(final String schedulerThreadNameFormat) {
        this(initScheduler(checkNotNull(schedulerThreadNameFormat)));
    }

    AbstractWatcherActor(final ScheduledExecutorService scheduler) {
        this.scheduler = checkNotNull(scheduler);
    }

    static ScheduledExecutorService initScheduler(final String nameFormat) {
        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setDaemon(true);
        builder.setNameFormat(nameFormat);
        return Executors.newScheduledThreadPool(1, builder.build());
    }

    final synchronized void submit(final Runnable taskToSubmit) {
        scheduler.schedule(taskToSubmit, 0, TimeUnit.SECONDS);
    }

    final synchronized void cancel(final ScheduledFuture<?> scheduledTask) {
        scheduledTask.cancel(true);
        try {
            getUninterruptibly(scheduledTask);
        } catch (CancellationException | ExecutionException ignored) { // NOSONAR
        }
    }

    final synchronized ScheduledFuture reschedule(final ScheduledFuture previousFuture,
                                                  final Runnable taskToSchedule,
                                                  final long periodSeconds) {
        if (previousFuture != null) {
            cancel(previousFuture);
        }
        return scheduler.scheduleAtFixedRate(taskToSchedule, 0, periodSeconds, TimeUnit.SECONDS);
    }

    final synchronized void closeScheduler() {
        scheduler.shutdownNow();
    }
}
