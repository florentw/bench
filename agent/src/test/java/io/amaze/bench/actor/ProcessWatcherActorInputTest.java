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

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.actor.ProcessWatcherActorInput.startSampling;
import static io.amaze.bench.actor.ProcessWatcherActorInput.stopSampling;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created on 9/10/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ProcessWatcherActorInputTest {

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(ProcessWatcherActorInput.class);
    }

    @Test
    public void equality() {
        EqualsTester tester = new EqualsTester();
        tester.addEqualityGroup(startSampling(1337, 2, "dummy"), startSampling(1337, 3, "dummy2"));
        tester.addEqualityGroup(startSampling(1338, 2, "dummy", ""), stopSampling(1338));
        tester.addEqualityGroup(stopSampling(10).hashCode(), startSampling(10, 2, "dummy").hashCode());

        tester.testEquals();
    }

    @Test
    public void serializable() {
        ProcessWatcherActorInput expected = startSampling(1337, 2, "dummy", "test");

        ProcessWatcherActorInput actual = SerializableTester.reserialize(expected);

        assertThat(actual.getCommand(), is(expected.getCommand()));
        assertThat(actual.getMetricKeyPrefix(), is(expected.getMetricKeyPrefix()));
        assertThat(actual.getMetricLabelSuffix(), is(expected.getMetricLabelSuffix()));
        assertThat(actual.getPeriodSeconds(), is(expected.getPeriodSeconds()));
        assertThat(actual.getPid(), is(expected.getPid()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void pid_must_be_greater_than_zero() {
        startSampling(0, 1, "dummy");
    }

    @Test(expected = IllegalArgumentException.class)
    public void period_must_be_greater_than_zero() {
        startSampling(1, 0, "dummy");
    }

    @Test(expected = IllegalArgumentException.class)
    public void key_cannot_be_empty() {
        startSampling(1, 1, "");
    }
}