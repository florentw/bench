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
package io.amaze.bench.shared.metric;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.shared.test.Json;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created on 8/28/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class SystemConfigTest {

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testConstructors(SystemConfig.class, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    public void serialize_deserialize() {
        SystemConfig expected = SystemConfigs.get();
        SystemConfig actual = SerializableTester.reserialize(expected);

        assertThat(expected.getHostName(), is(actual.getHostName()));
        assertThat(expected.getMemoryJson(), is(actual.getMemoryJson()));
        assertThat(expected.getOperatingSystemJson(), is(actual.getOperatingSystemJson()));
        assertThat(expected.getFileSystemJson(), is(actual.getFileSystemJson()));
        assertThat(expected.getProcessorJson(), is(actual.getProcessorJson()));
        assertThat(expected.toString(), is(actual.toString()));
    }

    @Test
    public void equality() {
        EqualsTester tester = new EqualsTester();
        tester.addEqualityGroup(SystemConfigs.get(), SystemConfigs.get());

        tester.testEquals();
    }

    @Test
    public void toString_yields_valid_json() {
        assertTrue(Json.isValid(SystemConfigs.get().toString()));
    }

}