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
package io.amaze.bench.orchestrator;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.client.runtime.actor.ActorConfig;
import io.amaze.bench.client.runtime.actor.DeployConfig;
import io.amaze.bench.client.runtime.actor.TestActor;
import io.amaze.bench.client.runtime.agent.AgentInputMessage;
import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import io.amaze.bench.orchestrator.registry.AgentRegistry;
import io.amaze.bench.orchestrator.registry.RegisteredAgent;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.metric.SystemConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_JSON_CONFIG;
import static io.amaze.bench.client.runtime.agent.AgentTest.DUMMY_AGENT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created on 4/3/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ResourceManagerTest {

    private static final String OTHER_DUMMY_AGENT = DUMMY_AGENT + "-other";
    private static final String OTHER_HOST = "other";

    private ResourceManager resourceManager;
    private AgentRegistry agentRegistry;
    @Mock
    private OrchestratorServer orchestratorServer;
    private ActorConfig defaultActorConfig;

    @Before
    public void before() {
        agentRegistry = new AgentRegistry();
        resourceManager = new ResourceManager(orchestratorServer, agentRegistry);

        defaultActorConfig = TestActor.DUMMY_CONFIG;
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(ResourceManager.class);
        tester.testAllPublicInstanceMethods(resourceManager);
    }

    @Test(expected = IllegalStateException.class)
    public void create_actor_no_agent_throws() {
        resourceManager.createActor(defaultActorConfig);
    }

    @Test
    public void create_actor_on_single_agent() {
        registerAgentOnOurHost();

        resourceManager.createActor(defaultActorConfig);

        verify(orchestratorServer).createActorQueue(DUMMY_ACTOR);
        verify(orchestratorServer).sendToAgent(any(AgentInputMessage.class));
        verifyNoMoreInteractions(orchestratorServer);

        assertThat(resourceManager.getActorsToAgents().size(), is(1));
        RegisteredAgent pickedAgent = resourceManager.getActorsToAgents().get(DUMMY_ACTOR);
        assertThat(pickedAgent.getName(), is(DUMMY_AGENT));
    }

    @Test
    public void create_and_close() {
        registerAgentOnOurHost();
        resourceManager.createActor(defaultActorConfig);

        resourceManager.closeActor(DUMMY_ACTOR);

        verify(orchestratorServer).createActorQueue(DUMMY_ACTOR);
        verify(orchestratorServer, times(2)).sendToAgent(any(AgentInputMessage.class));
        verify(orchestratorServer).deleteActorQueue(DUMMY_ACTOR);
        verifyNoMoreInteractions(orchestratorServer);

        assertThat(resourceManager.getActorsToAgents().isEmpty(), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void close_unknown_actor_throws() {
        resourceManager.closeActor(DUMMY_ACTOR);
    }

    @Test
    public void close_manager_closes_actor() {
        registerAgentOnOurHost();
        resourceManager.createActor(defaultActorConfig);

        resourceManager.close();

        verify(orchestratorServer).createActorQueue(DUMMY_ACTOR);
        verify(orchestratorServer, times(2)).sendToAgent(any(AgentInputMessage.class));
        verify(orchestratorServer).deleteActorQueue(DUMMY_ACTOR);
        verifyNoMoreInteractions(orchestratorServer);

        assertThat(resourceManager.getActorsToAgents().isEmpty(), is(true));
    }

    @Test
    public void create_actor_on_agent_on_preferred_host() {
        // Register first agent on preferred host (ours)
        AgentRegistrationMessage regMsgAgentOnPHost = registerAgentOnOurHost();

        // Registers second agent on another host
        registerDummyAgentOnOtherHost(OTHER_DUMMY_AGENT);

        List<String> preferredHosts = new ArrayList<>();
        preferredHosts.add(regMsgAgentOnPHost.getSystemConfig().getHostName());

        ActorConfig actorConfig = configWithPreferredHosts(preferredHosts);
        resourceManager.createActor(actorConfig);

        assertThat(resourceManager.getActorsToAgents().size(), is(1));
        RegisteredAgent pickedAgent = resourceManager.getActorsToAgents().get(DUMMY_ACTOR);
        assertThat(pickedAgent.getName(), is(DUMMY_AGENT));
    }

    @Test
    public void create_actor_with_preferred_host_but_fallback() {
        // Registers agent on another host
        registerAgentOnOurHost();

        List<String> preferredHosts = new ArrayList<>();
        preferredHosts.add(OTHER_HOST);

        ActorConfig actorConfig = configWithPreferredHosts(preferredHosts);
        resourceManager.createActor(actorConfig);

        assertThat(resourceManager.getActorsToAgents().size(), is(1));
        RegisteredAgent pickedAgent = resourceManager.getActorsToAgents().get(DUMMY_ACTOR);
        assertThat(pickedAgent.getName(), is(DUMMY_AGENT));
    }

    @Test
    public void create_actor_no_preferred_host_picks_one_agent() {
        registerAgentOnOurHost(); // Agent 1
        registerDummyAgentOnOtherHost(OTHER_DUMMY_AGENT); // Agent 2
        registerDummyAgentOnOtherHost(OTHER_DUMMY_AGENT + "-2"); // Agent 3
        registerDummyAgentOnOtherHost(OTHER_DUMMY_AGENT + "-3"); // Agent 3

        List<String> preferredHosts = Collections.emptyList();

        ActorConfig actorConfig = configWithPreferredHosts(preferredHosts);
        resourceManager.createActor(actorConfig);

        assertThat(resourceManager.getActorsToAgents().size(), is(1));
        RegisteredAgent pickedAgent = resourceManager.getActorsToAgents().get(DUMMY_ACTOR);
        assertNotNull(pickedAgent);
    }

    private ActorConfig configWithPreferredHosts(final List<String> preferredHosts) {
        JMSEndpoint endpoint = new JMSEndpoint("", 12);
        return new ActorConfig(DUMMY_ACTOR, "", new DeployConfig(endpoint, false, preferredHosts), DUMMY_JSON_CONFIG);
    }

    private AgentRegistrationMessage registerAgentOnOurHost() {
        AgentRegistrationMessage regMsgAgentOnPHost = AgentRegistrationMessage.create(DUMMY_AGENT);
        agentRegistry.getListenerForOrchestrator().onAgentRegistration(regMsgAgentOnPHost);
        return regMsgAgentOnPHost;
    }

    private AgentRegistrationMessage registerDummyAgentOnOtherHost(final String agentName) {
        SystemConfig sysInfoOtherHost = SystemConfig.createWithHostname(OTHER_HOST);
        AgentRegistrationMessage regMsgAgentOnOtherHost = new AgentRegistrationMessage(agentName, sysInfoOtherHost, 0);
        agentRegistry.getListenerForOrchestrator().onAgentRegistration(regMsgAgentOnOtherHost);
        return regMsgAgentOnOtherHost;
    }

}