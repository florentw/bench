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
package io.amaze.bench.runtime.agent;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.runtime.cluster.ActorCreationRequest;
import io.amaze.bench.shared.test.Json;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created on 8/15/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AgentInputMessageTest {

    private AgentInputMessage inputMessage;

    @Before
    public void before() {
        inputMessage = AgentInputMessage.createActor("agent", new ActorCreationRequest(TestActor.DUMMY_CONFIG));
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(AgentInputMessage.class);
        tester.testAllPublicInstanceMethods(inputMessage);
    }

    @Test
    public void serialize_deserialize() {
        AgentInputMessage received = SerializableTester.reserialize(inputMessage);

        assertThat(received.getAction(), is(inputMessage.getAction()));
        assertThat(received.getCreationRequest(), is(inputMessage.getCreationRequest()));
        assertThat(received.getTargetAgent(), is(inputMessage.getTargetAgent()));
    }

    @Test
    public void toString_yields_valid_json() {
        assertTrue(Json.isValid(inputMessage.toString()));
    }

}