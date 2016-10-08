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
package io.amaze.bench.shared.jgroups;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.shared.test.Json;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created on 10/2/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsStateKeyTest {

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(JgroupsStateKey.class);
    }

    @Test
    public void serializable() {
        JgroupsStateKey expected = makeKey();
        JgroupsStateKey actual = SerializableTester.reserialize(expected);

        assertThat(expected, is(actual));
    }

    @Test
    public void equality() {
        EqualsTester tester = new EqualsTester();
        tester.addEqualityGroup(makeKey(), makeKey());
        tester.addEqualityGroup(makeKey().hashCode(), makeKey().hashCode());
        tester.testEquals();
    }

    @Test
    public void toString_yields_valid_json() {
        assertTrue(Json.isValid(makeKey().toString()));
    }

    private JgroupsStateKey makeKey() {
        return new JgroupsStateKey("key");
    }

}