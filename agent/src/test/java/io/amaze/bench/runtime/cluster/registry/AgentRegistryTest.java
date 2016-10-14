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
package io.amaze.bench.runtime.cluster.registry;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.agent.AgentRegistrationMessage;
import io.amaze.bench.shared.metric.SystemConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import static io.amaze.bench.runtime.agent.AgentTest.DUMMY_AGENT;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created on 4/2/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AgentRegistryTest {

    private AgentRegistrationMessage regMsg;
    private AgentRegistry registry;
    private AgentRegistryListener clusterListener;

    @Mock
    private AgentRegistryListener clientListener;
    @Mock
    private Endpoint endpoint;

    @Before
    public void before() {
        registry = new AgentRegistry();
        clusterListener = registry.createClusterListener();
        regMsg = AgentRegistrationMessage.create(DUMMY_AGENT, endpoint);

        registry.addListener(clientListener);
    }

    @After
    public void after() {
        registry.removeListener(clientListener);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(AgentRegistry.class);
        tester.testAllPublicInstanceMethods(registry);
    }

    @Test
    public void resetState_overrides_current_view() {
        clusterListener.onAgentRegistration(regMsg);
        Set<RegisteredAgent> newAgentSet = new HashSet<>();
        RegisteredAgent newAgent = new RegisteredAgent(DUMMY_AGENT,
                                                       SystemConfig.createWithHostname("dummy"),
                                                       0,
                                                       endpoint);
        newAgentSet.add(newAgent);

        registry.resetState(newAgentSet);

        assertThat(registry.all(), is(newAgentSet));
    }

    @Test
    public void agent_registered() {
        clusterListener.onAgentRegistration(regMsg);

        RegisteredAgent agent = registry.byName(DUMMY_AGENT);
        assertThat(agent.getAgentName(), is(DUMMY_AGENT));
        assertThat(agent.getSystemConfig(), is(regMsg.getSystemConfig()));
        assertThat(agent.getCreationTime(), is(regMsg.getCreationTime()));

        verify(clientListener).onAgentRegistration(regMsg);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void agent_registered_signs_off() {
        clusterListener.onAgentRegistration(regMsg);
        clusterListener.onAgentSignOff(DUMMY_AGENT);

        RegisteredAgent agent = registry.byName(DUMMY_AGENT);
        assertNull(agent);

        verify(clientListener).onAgentRegistration(regMsg);
        verify(clientListener).onAgentSignOff(DUMMY_AGENT);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void unknown_agent_signs_off() {
        clusterListener.onAgentSignOff(DUMMY_AGENT);

        RegisteredAgent agent = registry.byName(DUMMY_AGENT);
        assertNull(agent);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void list_all() {
        clusterListener.onAgentRegistration(regMsg);

        Set<RegisteredAgent> agents = registry.all();

        assertThat(agents.size(), is(1));

        RegisteredAgent agent = agents.iterator().next();
        assertThat(agent.getAgentName(), is(DUMMY_AGENT));
    }

    @Test
    public void removed_listener_is_not_notified() {
        registry.removeListener(clientListener);

        clusterListener.onAgentRegistration(regMsg);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void remove_listener_twice_does_not_throw() {
        registry.removeListener(clientListener);
        registry.removeListener(clientListener);
    }
}