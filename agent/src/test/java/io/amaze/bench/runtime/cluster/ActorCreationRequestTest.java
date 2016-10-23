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
package io.amaze.bench.runtime.cluster;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.shared.test.Json;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_CONFIG;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created on 9/30/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ActorCreationRequestTest {

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(ActorCreationRequest.class);
    }

    @Test
    public void serializable() {
        ActorCreationRequest expected = makeRequest();
        ActorCreationRequest actual = SerializableTester.reserialize(expected);

        assertThat(expected.getActorConfig(), is(actual.getActorConfig()));
    }

    @Test
    public void equality() {
        EqualsTester tester = new EqualsTester();
        tester.addEqualityGroup(makeRequest(), makeRequest());
        tester.testEquals();
    }

    @Test
    public void toString_yields_valid_json() {
        assertTrue(Json.isValid(makeRequest().toString()));
    }

    private ActorCreationRequest makeRequest() {
        return new ActorCreationRequest(DUMMY_CONFIG);
    }
}