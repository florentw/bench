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
package io.amaze.bench.runtime.cluster.actor;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.shared.test.Json;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created on 9/16/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ActorInputMessageTest {

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(ActorInputMessage.class);
        tester.testAllPublicStaticMethods(ActorInputMessage.class);
    }

    @Test
    public void serializable() {
        ActorInputMessage expected = ActorInputMessage.close();
        ActorInputMessage actual = SerializableTester.reserialize(expected);

        assertThat(actual.getCommand(), is(expected.getCommand()));
        assertThat(actual.getFrom(), is(expected.getFrom()));
        assertThat(actual.getPayload(), is(expected.getPayload()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void from_should_not_be_empty() {
        ActorInputMessage.sendMessage("", "payload");
    }

    @Test
    public void equality() {
        EqualsTester tester = new EqualsTester();
        tester.addEqualityGroup(ActorInputMessage.close(), ActorInputMessage.close());
        tester.addEqualityGroup(ActorInputMessage.dumpMetrics(), ActorInputMessage.dumpMetrics());
        tester.testEquals();
    }

    @Test
    public void toString_yields_valid_json() {
        assertTrue(Json.isValid(ActorInputMessage.close().toString()));
        assertTrue(Json.isValid(ActorInputMessage.dumpMetrics().toString()));
        assertTrue(Json.isValid(ActorInputMessage.dumpMetrics().toString()));
        assertTrue(Json.isValid(ActorInputMessage.sendMessage("from", "payload").toString()));
    }

}