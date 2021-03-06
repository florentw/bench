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
package io.amaze.bench.cluster.agent;

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
 * Created on 10/29/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AgentKeyTest {

    private static final AgentKey AGENT_KEY = new AgentKey("dummy");

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(AgentKey.class);
    }

    @Test
    public void equality() {
        EqualsTester tester = new EqualsTester();
        tester.addEqualityGroup(AGENT_KEY, AGENT_KEY);
        tester.testEquals();
    }

    @Test
    public void serializable() {
        AgentKey expected = AGENT_KEY;
        AgentKey actual = SerializableTester.reserialize(expected);
        assertThat(expected.getName(), is(actual.getName()));
        assertThat(expected, is(actual));
    }

    @Test
    public void toString_yields_valid_json() {
        assertTrue(Json.isValid(AGENT_KEY.toString()));
    }


}