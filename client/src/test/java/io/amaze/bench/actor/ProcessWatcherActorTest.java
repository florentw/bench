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
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import io.amaze.bench.client.runtime.actor.ActorValidators;
import io.amaze.bench.client.runtime.actor.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.amaze.bench.actor.ProcessWatcherActorInput.startSampling;
import static io.amaze.bench.actor.ProcessWatcherActorInput.stopSampling;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 9/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ProcessWatcherActorTest {

    private static final String KEY_PREFIX = "my process";
    private static final int PID = 10;

    @Mock
    private Metrics metrics;
    @Mock
    private ScheduledExecutorService scheduler;
    private ProcessWatcherActor watcherActor;

    @Before
    public void init() {
        watcherActor = new ProcessWatcherActor(metrics, scheduler);
    }

    @Test
    public void actor_class_is_valid() throws ValidationException {
        ActorValidators.get().loadAndValidate(ProcessWatcherActor.class.getName());
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ProcessWatcherActorInput.class, stopSampling(PID));
        tester.testAllPublicConstructors(ProcessWatcherActor.class);
        tester.testAllPublicInstanceMethods(watcherActor);
    }

    @Test
    public void create_actor_using_public_constructor_does_not_throw() {
        ProcessWatcherActor actorWithExecutor = new ProcessWatcherActor(metrics);
        actorWithExecutor.closeThreads();
    }

    @Test
    public void sampling_real_process_produces_metrics() throws ReactorException {
        assumeTrue(ownPid() > 0);
        ProcessWatcherActor actorWithExecutor = new ProcessWatcherActor(metrics);
        Metrics.Sink mockedSink = mock(Metrics.Sink.class);
        when(metrics.sinkFor(any(Metric.class))).thenReturn(mockedSink);

        actorWithExecutor.onMessage("test", startSampling(ownPid(), 1, "test", "label"));
        sleepUninterruptibly(5, TimeUnit.SECONDS);
        actorWithExecutor.onMessage("test", stopSampling(ownPid()));

        verify(mockedSink, atLeastOnce()).timed(anyLong(), any(Number.class));
        actorWithExecutor.closeThreads();
    }

    @Test
    public void schedule_periodic_task_on_start_sampling_message() throws ReactorException {
        watcherActor.onMessage("test", startSampling(PID, 1, KEY_PREFIX));

        verify(metrics, times(5)).sinkFor(any(Metric.class));
        verify(scheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(1L), eq(TimeUnit.SECONDS));
        verifyNoMoreInteractions(metrics);
        verifyNoMoreInteractions(scheduler);
    }

    @Test
    public void reschedule_periodic_task_on_second_start_sampling_message() throws ReactorException {
        ScheduledFuture mockedFuture = mockedFuture();
        watcherActor.onMessage("test", startSampling(PID, 1, KEY_PREFIX));

        watcherActor.onMessage("test", startSampling(PID, 11, KEY_PREFIX));

        InOrder inOrder = inOrder(scheduler, mockedFuture);
        inOrder.verify(scheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(1L), eq(TimeUnit.SECONDS));
        inOrder.verify(mockedFuture).cancel(true);
        inOrder.verify(scheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(11L), eq(TimeUnit.SECONDS));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void cancel_task_on_stop_message() throws ReactorException, ExecutionException, InterruptedException {
        ScheduledFuture mockedFuture = mockedFuture();
        watcherActor.onMessage("test", startSampling(PID, 1, KEY_PREFIX));

        watcherActor.onMessage("test", stopSampling(PID));

        InOrder inOrder = inOrder(scheduler, mockedFuture);
        inOrder.verify(scheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(1L), eq(TimeUnit.SECONDS));
        inOrder.verify(mockedFuture).cancel(true);
        inOrder.verify(mockedFuture).get();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void execution_exception_is_ignored() throws ReactorException, ExecutionException, InterruptedException {
        ScheduledFuture mockedFuture = mockedFuture();
        ExecutionException executionException = new ExecutionException(new RuntimeException());
        when(mockedFuture.get()).thenThrow(executionException);
        watcherActor.onMessage("test", startSampling(PID, 1, KEY_PREFIX));

        watcherActor.onMessage("test", stopSampling(PID));

        InOrder inOrder = inOrder(scheduler, mockedFuture);
        inOrder.verify(scheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(1L), eq(TimeUnit.SECONDS));
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

    private int ownPid() {
        String[] tokens = ManagementFactory.getRuntimeMXBean().getName().split("@");
        if (tokens.length != 2) {
            return -1;
        }
        try {
            return Integer.parseInt(tokens[0]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private ScheduledFuture mockedFuture() {
        ScheduledFuture mockedFuture = mock(ScheduledFuture.class);
        when(scheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class))).
                thenReturn(mockedFuture);
        return mockedFuture;
    }
}