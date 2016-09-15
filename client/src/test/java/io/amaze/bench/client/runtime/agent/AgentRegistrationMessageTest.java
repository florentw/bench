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
package io.amaze.bench.client.runtime.agent;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.shared.metric.SystemConfig;
import io.amaze.bench.shared.metric.SystemConfigs;
import io.amaze.bench.shared.test.Json;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created on 3/5/16.
 */
public final class AgentRegistrationMessageTest {

    private static final String AGENT_NAME = "dummy-agent";
    private AgentRegistrationMessage msg;

    @Before
    public void before() {
        msg = AgentRegistrationMessage.create(AGENT_NAME);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(SystemConfig.class, SystemConfigs.get());
        tester.testAllPublicConstructors(AgentRegistrationMessage.class);
        tester.testAllPublicStaticMethods(AgentRegistrationMessage.class);
    }

    @Test
    public void create_msg() {
        assertTrue(msg.getName().equals(AGENT_NAME));
        assertNotNull(msg.getSystemConfig());
        assertTrue(msg.getCreationTime() > 0);
    }

    @Test
    public void equality() {
        AgentRegistrationMessage other = AgentRegistrationMessage.create("different");

        new EqualsTester() //
                .addEqualityGroup(msg, msg) //
                .addEqualityGroup(msg.getName(), msg.getName()) //
                .addEqualityGroup(msg.hashCode(), msg.hashCode()) //
                .testEquals();

        assertThat(msg, is(not(other)));
    }

    @Test
    public void serialize_deserialize() {
        AgentRegistrationMessage received = SerializableTester.reserialize(msg);

        assertThat(received.getCreationTime(), is(msg.getCreationTime()));
        assertThat(received.getName(), is(msg.getName()));
        assertNotNull(received.getSystemConfig());
    }

    @Test
    public void toString_yields_valid_json() {
        assertTrue(Json.isValid(msg.toString()));
    }

}