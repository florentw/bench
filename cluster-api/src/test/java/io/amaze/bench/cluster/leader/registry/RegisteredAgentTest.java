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
package io.amaze.bench.cluster.leader.registry;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.agent.AgentUtil;
import io.amaze.bench.shared.metric.SystemConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created on 10/14/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class RegisteredAgentTest {

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(SystemConfig.class, SystemConfig.createWithHostname("dummy"));
        tester.setDefault(AgentKey.class, AgentUtil.DUMMY_AGENT);

        tester.testAllPublicConstructors(RegisteredAgent.class);
    }

    @Test
    public void serializable() {
        RegisteredAgent expected = registeredAgent();
        RegisteredAgent actual = SerializableTester.reserialize(expected);

        assertThat(expected.getAgentKey(), is(actual.getAgentKey()));
        assertThat(expected.getCreationTime(), is(actual.getCreationTime()));
        assertThat(expected.getEndpoint(), is(actual.getEndpoint()));
        assertThat(expected.getSystemConfig(), is(actual.getSystemConfig()));
    }

    @Test
    public void equality() {
        EqualsTester tester = new EqualsTester();
        tester.addEqualityGroup(registeredAgent(), registeredAgent());

        tester.testEquals();
    }

    private RegisteredAgent registeredAgent() {
        return new RegisteredAgent(AgentUtil.DUMMY_AGENT,
                                   SystemConfig.createWithHostname("dummy"),
                                   0,
                                   new RegisteredActorTest.DummyEndpoint("endpoint"));
    }

}