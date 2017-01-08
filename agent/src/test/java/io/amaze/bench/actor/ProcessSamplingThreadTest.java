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

import io.amaze.bench.api.Before;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.api.metric.Metrics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import oshi.json.SystemInfo;

import static io.amaze.bench.actor.ProcessWatcherActorInput.startSampling;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Created on 9/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ProcessSamplingThreadTest {

    private static final int PID = 6666666;

    @Mock
    private Metrics metrics;
    @Mock
    private Metrics.Sink sink;
    @Mock
    private SystemInfo systemInfo;

    @Before
    public void initSink() {
        when(metrics.sinkFor(any(Metric.class))).thenReturn(sink);
    }

    @Test
    public void sampling_non_existent_pid_does_nothing() {
        ProcessSamplingThread thread = new ProcessSamplingThread(metrics, systemInfo, startSampling(PID, 1, "prefix"));

        thread.run();

        verifyZeroInteractions(sink);
    }

}