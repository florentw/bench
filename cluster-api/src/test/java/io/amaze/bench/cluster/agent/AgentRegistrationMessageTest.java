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
package io.amaze.bench.cluster.agent;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.cluster.registry.RegisteredActorTest;
import io.amaze.bench.shared.test.Json;
import io.amaze.bench.shared.util.SystemConfig;
import io.amaze.bench.shared.util.SystemConfigs;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created on 3/5/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AgentRegistrationMessageTest {

    private static final AgentKey AGENT = new AgentKey("dummy-agent");

    private AgentRegistrationMessage msg;
    private RegisteredActorTest.DummyEndpoint endpoint;

    @Before
    public void before() {
        endpoint = new RegisteredActorTest.DummyEndpoint("endpoint");
        msg = AgentRegistrationMessage.create(AGENT, endpoint);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(SystemConfig.class, SystemConfigs.get());
        tester.setDefault(AgentKey.class, new AgentKey("agent"));

        tester.testAllPublicConstructors(AgentRegistrationMessage.class);
        tester.testAllPublicStaticMethods(AgentRegistrationMessage.class);
    }

    @Test
    public void create_msg() {
        assertTrue(msg.getKey().equals(AGENT));
        assertNotNull(msg.getSystemConfig());
        assertTrue(msg.getCreationTime() > 0);
    }

    @Test
    public void equality() {
        AgentRegistrationMessage other = AgentRegistrationMessage.create(new AgentKey("different"), endpoint);

        new EqualsTester() //
                .addEqualityGroup(msg, msg) //
                .addEqualityGroup(msg.getKey(), msg.getKey()) //
                .addEqualityGroup(msg.hashCode(), msg.hashCode()) //
                .testEquals();

        assertThat(msg, is(CoreMatchers.not(other)));
    }

    @Test
    public void serialize_deserialize() {
        AgentRegistrationMessage received = SerializableTester.reserialize(msg);

        assertThat(received.getCreationTime(), is(msg.getCreationTime()));
        assertThat(received.getKey(), is(msg.getKey()));
        assertThat(received.getEndpoint(), is(msg.getEndpoint()));
        assertNotNull(received.getSystemConfig());
    }

    @Test
    public void toString_yields_valid_json() {
        assertTrue(Json.isValid(msg.toString()));
    }

}