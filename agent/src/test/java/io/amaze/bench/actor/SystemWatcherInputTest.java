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

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.actor.SystemWatcherInput.Command;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created on 9/5/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class SystemWatcherInputTest {

    private static final long PERIOD_SECONDS = 10L;
    private static final SystemWatcherInput DUMMY_INPUT = SystemWatcherInput.setPeriod(PERIOD_SECONDS);

    @Test
    public void serializable() {
        SystemWatcherInput actual = SerializableTester.reserialize(DUMMY_INPUT);

        assertThat(actual.getCommand(), is(Command.SET_PERIOD));
        assertThat(actual.getPeriodSeconds(), is(PERIOD_SECONDS));
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicInstanceMethods(DUMMY_INPUT);
        tester.testAllPublicStaticMethods(SystemWatcherInput.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void period_cant_be_less_than_one_second() {
        SystemWatcherInput.setPeriod(0);
    }
}