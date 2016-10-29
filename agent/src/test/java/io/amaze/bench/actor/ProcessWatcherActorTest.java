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

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import io.amaze.bench.api.ReactorException;
import io.amaze.bench.api.RecoverableException;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import io.amaze.bench.runtime.actor.ActorValidators;
import io.amaze.bench.runtime.cluster.actor.ValidationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import oshi.json.SystemInfo;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.amaze.bench.actor.ProcessWatcherActorInput.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 9/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ProcessWatcherActorTest {

    private static final String KEY_PREFIX = "myProcess";
    private static final String LABEL_SUFFIX = "myLabel";
    private static final int PID = 66666666;
    private static final String FROM = "from";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
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
        Metrics.Sink mockedSink = mockedSink();

        actorWithExecutor.onMessage(FROM, startSampling(ownPid(), 1, FROM, LABEL_SUFFIX));
        sleepUninterruptibly(5, TimeUnit.SECONDS);
        actorWithExecutor.onMessage(FROM, stopSampling(ownPid()));

        verify(mockedSink, atLeastOnce()).timed(anyLong(), any(Number.class));
        actorWithExecutor.closeThreads();
    }

    @Test
    public void schedule_periodic_task_on_start_sampling_message() throws ReactorException {
        watcherActor.onMessage(FROM, startSampling(PID, 1, KEY_PREFIX));

        verify(metrics, times(5)).sinkFor(any(Metric.class));
        verify(scheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(1L), eq(TimeUnit.SECONDS));
        verifyNoMoreInteractions(metrics);
        verifyNoMoreInteractions(scheduler);
    }

    @Test
    public void reschedule_periodic_task_on_second_start_sampling_message() throws ReactorException {
        ScheduledFuture mockedFuture = mockedFuture();
        watcherActor.onMessage(FROM, startSampling(PID, 1, KEY_PREFIX));

        watcherActor.onMessage(FROM, startSampling(PID, 11, KEY_PREFIX));

        InOrder inOrder = inOrder(scheduler, mockedFuture);
        inOrder.verify(scheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(1L), eq(TimeUnit.SECONDS));
        inOrder.verify(mockedFuture).cancel(true);
        inOrder.verify(scheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(11L), eq(TimeUnit.SECONDS));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void cancel_task_on_stop_message() throws ReactorException, ExecutionException, InterruptedException {
        ScheduledFuture mockedFuture = mockedFuture();
        watcherActor.onMessage(FROM, startSampling(PID, 1, KEY_PREFIX));

        watcherActor.onMessage(FROM, stopSampling(PID));

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
        watcherActor.onMessage(FROM, startSampling(PID, 1, KEY_PREFIX));

        watcherActor.onMessage(FROM, stopSampling(PID));

        InOrder inOrder = inOrder(scheduler, mockedFuture);
        inOrder.verify(scheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(1L), eq(TimeUnit.SECONDS));
        inOrder.verify(mockedFuture).cancel(true);
        inOrder.verify(mockedFuture).get();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void produces_metrics_for_own_process_on_start_stopwatch() throws ReactorException {
        ProcessWatcherActor actorWithExecutor = new ProcessWatcherActor(metrics);
        Metrics.Sink mockedSink = mockedSink();

        actorWithExecutor.onMessage(FROM, startStopwatch(ownPid(), KEY_PREFIX));

        verify(metrics, timeout(2000).times(4)).sinkFor(any(Metric.class));
        verify(mockedSink, times(4)).timed(anyLong(), anyLong());
        verifyNoMoreInteractions(metrics);
        verifyNoMoreInteractions(mockedSink);
        actorWithExecutor.closeThreads();
    }

    @Test
    public void produces_metrics_for_own_process_on_start_stop_stopwatch() throws ReactorException {
        ProcessWatcherActor actorWithExecutor = new ProcessWatcherActor(metrics);
        Metrics.Sink mockedSink = mockedSink();

        actorWithExecutor.onMessage(FROM, startStopwatch(ownPid(), KEY_PREFIX, LABEL_SUFFIX));
        sleepUninterruptibly(1, TimeUnit.SECONDS);
        actorWithExecutor.onMessage(FROM, stopStopwatch(ownPid(), KEY_PREFIX));

        verify(metrics, timeout(2000).times(13)).sinkFor(any(Metric.class));
        verify(mockedSink, times(8)).timed(anyLong(), anyLong());
        verify(mockedSink, times(5)).add(anyLong());
        verifyNoMoreInteractions(metrics);
        verifyNoMoreInteractions(mockedSink);
        actorWithExecutor.closeThreads();
    }

    @Test
    public void schedule_thread_when_start_stopwatch_called() throws ReactorException {
        watcherActor.onMessage(FROM, startStopwatch(PID, KEY_PREFIX));

        verify(scheduler).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));
        verifyNoMoreInteractions(scheduler);
    }

    @Test
    public void start_sampling_on_invalid_pid_does_nothing() throws ReactorException {
        ProcessWatcherActor actorWithExecutor = new ProcessWatcherActor(metrics);
        Metrics.Sink mockedSink = mockedSink();

        actorWithExecutor.onMessage(FROM, startSampling(PID, 1, KEY_PREFIX));
        sleepUninterruptibly(1, TimeUnit.SECONDS);

        verifyZeroInteractions(scheduler, mockedSink);
    }

    @Test
    public void throw_RecoverableException_when_stop_sampling_called_twice() throws ReactorException {
        mockedFuture();
        watcherActor.onMessage(FROM, startSampling(PID, 1, KEY_PREFIX));
        watcherActor.onMessage(FROM, stopSampling(PID));

        expectedException.expect(RecoverableException.class);
        watcherActor.onMessage(FROM, stopSampling(PID));

        verify(scheduler).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));
        verifyNoMoreInteractions(scheduler);
    }

    @Test
    public void start_stopwatch_on_invalid_pid_does_nothing() throws ReactorException {
        ProcessWatcherActor actorWithExecutor = new ProcessWatcherActor(metrics);
        Metrics.Sink mockedSink = mockedSink();

        actorWithExecutor.onMessage(FROM, startStopwatch(PID, KEY_PREFIX));
        sleepUninterruptibly(1, TimeUnit.SECONDS);

        verifyZeroInteractions(metrics, scheduler, mockedSink);
    }

    @Test
    public void throws_RecoverableException_when_start_stopwatch_called_twice() throws ReactorException {
        ProcessWatcherActor actorWithExecutor = new ProcessWatcherActor(metrics);
        actorWithExecutor.onMessage(FROM, startStopwatch(PID, KEY_PREFIX));
        actorWithExecutor.onMessage(FROM, stopStopwatch(PID, KEY_PREFIX));

        expectedException.expect(RecoverableException.class);
        actorWithExecutor.onMessage(FROM, stopStopwatch(PID, KEY_PREFIX));

        verify(scheduler).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));
        verifyNoMoreInteractions(scheduler);
    }

    @Test
    public void throws_RecoverableException_when_stop_stopwatch_called_twice() throws ReactorException {
        watcherActor.onMessage(FROM, startStopwatch(PID, KEY_PREFIX));

        expectedException.expect(RecoverableException.class);
        watcherActor.onMessage(FROM, startStopwatch(PID, KEY_PREFIX));

        verify(scheduler).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));
        verifyNoMoreInteractions(scheduler);
    }

    @Test
    public void closeThreads_closes_executor() {
        watcherActor.closeThreads();

        verify(scheduler).shutdownNow();
        verifyNoMoreInteractions(scheduler);
    }

    @Test
    public void stopWatchKey_equality() {
        EqualsTester tester = new EqualsTester();
        tester.addEqualityGroup(new ProcessWatcherActor.StopWatchKey(PID, KEY_PREFIX),
                                new ProcessWatcherActor.StopWatchKey(PID, KEY_PREFIX));
        tester.testEquals();
    }

    private Metrics.Sink mockedSink() {
        Metrics.Sink mockedSink = mock(Metrics.Sink.class);
        when(metrics.sinkFor(any(Metric.class))).thenReturn(mockedSink);
        return mockedSink;
    }

    private int ownPid() {
        return new SystemInfo().getOperatingSystem().getProcessId();
    }

    private ScheduledFuture mockedFuture() {
        ScheduledFuture mockedFuture = mock(ScheduledFuture.class);
        when(scheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class))).
                thenReturn(mockedFuture);
        return mockedFuture;
    }
}