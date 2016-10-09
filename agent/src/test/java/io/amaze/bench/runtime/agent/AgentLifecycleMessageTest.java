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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created on 8/15/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AgentLifecycleMessageTest {

    private static final String AGENT = "agent";
    private AgentLifecycleMessage outputMessage;

    @Before
    public void before() {
        outputMessage = AgentLifecycleMessage.closed(AGENT);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(AgentLifecycleMessage.class);
        tester.testAllPublicStaticMethods(AgentLifecycleMessage.class);
        tester.testAllPublicInstanceMethods(outputMessage);
    }

    @Test
    public void created_returns_instance_of_AgentLifecycleMessage() {
        AgentRegistrationMessage registrationMessage = AgentRegistrationMessage.create(AGENT);
        AgentLifecycleMessage message = AgentLifecycleMessage.created(registrationMessage);

        assertThat(message.getState(), is(AgentLifecycleMessage.State.CREATED));
        assertThat(message.getAgent(), is(AGENT));
        assertThat(message.getRegistrationMessage(), is(registrationMessage));
    }

    @Test
    public void closed_returns_instance_of_AgentLifecycleMessage() {
        AgentLifecycleMessage message = AgentLifecycleMessage.closed(AGENT);

        assertThat(message.getState(), is(AgentLifecycleMessage.State.CLOSED));
        assertThat(message.getAgent(), is(AGENT));
        assertNull(message.getRegistrationMessage());
    }

    @Test
    public void serialize_deserialize() {
        AgentLifecycleMessage received = SerializableTester.reserialize(outputMessage);

        assertThat(received.getState(), is(outputMessage.getState()));
        assertThat(received.getAgent(), is(outputMessage.getAgent()));
        assertThat(received.getRegistrationMessage(), is(outputMessage.getRegistrationMessage()));
    }

}