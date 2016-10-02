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

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.api.ReactorException;
import io.amaze.bench.api.metric.Metrics;
import io.amaze.bench.runtime.actor.ActorValidators;
import io.amaze.bench.runtime.actor.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.amaze.bench.actor.SystemWatcherActor.*;
import static io.amaze.bench.actor.SystemWatcherInput.start;
import static io.amaze.bench.actor.SystemWatcherInput.stop;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 9/7/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class SystemWatcherActorTest {

    @Mock
    private ScheduledExecutorService scheduler;
    @Mock
    private Metrics metrics;
    @Mock
    private Metrics.Sink sink;

    private SystemWatcherActor watcherActor;

    @Before
    public void init() {
        watcherActor = new SystemWatcherActor(metrics, scheduler);
    }

    @Test
    public void actor_class_is_valid() throws ValidationException {
        ActorValidators.get().loadAndValidate(SystemWatcherActor.class.getName());
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        SystemWatcherInput watcherInput = start(10);
        tester.setDefault(SystemWatcherInput.class, watcherInput);

        tester.testAllPublicConstructors(SystemWatcherActor.class);
        tester.testAllPublicInstanceMethods(watcherActor);
    }

    @Test
    public void create_actor_using_public_constructor_does_not_throw() {
        SystemWatcherActor actorWithExecutor = new SystemWatcherActor(metrics);
        actorWithExecutor.closeThreads();
    }

    @Test
    public void actor_with_real_executor_produces_metrics_once_started() throws ReactorException {
        when(metrics.sinkFor(METRIC_AVAILABLE_RAM)).thenReturn(sink);
        when(metrics.sinkFor(METRIC_CPU_USAGE)).thenReturn(sink);
        when(metrics.sinkFor(METRIC_LOAD_AVERAGE)).thenReturn(sink);
        when(metrics.sinkFor(METRIC_SWAP_USED)).thenReturn(sink);
        SystemWatcherActor actorWithExecutor = new SystemWatcherActor(metrics);

        actorWithExecutor.onMessage("test", start(1));
        sleepUninterruptibly(5, TimeUnit.SECONDS);

        verify(sink, atLeastOnce()).timed(anyLong(), any(Number.class));

        actorWithExecutor.closeThreads();
    }

    @Test
    public void schedule_periodic_task_on_start_message() throws ReactorException {
        watcherActor.onMessage("test", start(10));

        verify(metrics).sinkFor(METRIC_LOAD_AVERAGE);
        verify(metrics).sinkFor(METRIC_CPU_USAGE);
        verify(metrics).sinkFor(METRIC_SWAP_USED);
        verify(metrics).sinkFor(METRIC_AVAILABLE_RAM);
        verify(scheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(10L), eq(TimeUnit.SECONDS));
        verifyNoMoreInteractions(metrics);
        verifyNoMoreInteractions(scheduler);
    }

    @Test
    public void reschedule_periodic_task_on_set_period_message() throws ReactorException {
        ScheduledFuture mockedFuture = mockedFuture();
        watcherActor.onMessage("test", start(10));

        watcherActor.onMessage("test", start(20));

        InOrder inOrder = inOrder(scheduler, mockedFuture);
        inOrder.verify(scheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(10L), eq(TimeUnit.SECONDS));
        inOrder.verify(mockedFuture).cancel(true);
        inOrder.verify(scheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(20L), eq(TimeUnit.SECONDS));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void cancel_previous_task_on_stop_message()
            throws ReactorException, ExecutionException, InterruptedException {
        ScheduledFuture mockedFuture = mockedFuture();
        watcherActor.onMessage("test", start(10));

        watcherActor.onMessage("test", stop());

        InOrder inOrder = inOrder(scheduler, mockedFuture);
        inOrder.verify(scheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(10L), eq(TimeUnit.SECONDS));
        inOrder.verify(mockedFuture).cancel(true);
        inOrder.verify(mockedFuture).get();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void execution_exception_is_ignored() throws ReactorException, ExecutionException, InterruptedException {
        ScheduledFuture mockedFuture = mockedFuture();
        ExecutionException executionException = new ExecutionException(new RuntimeException());
        when(mockedFuture.get()).thenThrow(executionException);
        watcherActor.onMessage("test", start(10));

        watcherActor.onMessage("test", stop());

        InOrder inOrder = inOrder(scheduler, mockedFuture);
        inOrder.verify(scheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(10L), eq(TimeUnit.SECONDS));
        inOrder.verify(mockedFuture).cancel(true);
        inOrder.verify(mockedFuture).get();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void closeThreads_closes_executor() {
        watcherActor.closeThreads();

        verify(scheduler).shutdownNow();
        verifyNoMoreInteractions(scheduler);
    }

    private ScheduledFuture mockedFuture() {
        ScheduledFuture mockedFuture = mock(ScheduledFuture.class);
        when(scheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class))).
                thenReturn(mockedFuture);
        return mockedFuture;
    }

}