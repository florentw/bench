package io.amaze.bench.orchestrator;

import io.amaze.bench.client.runtime.agent.AgentInputMessage;
import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import io.amaze.bench.orchestrator.registry.AgentRegistry;
import io.amaze.bench.orchestrator.registry.RegisteredAgent;
import io.amaze.bench.shared.metric.SystemInfo;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.amaze.bench.TestConstants.DUMMY_ACTOR;
import static io.amaze.bench.TestConstants.DUMMY_AGENT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created on 4/3/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class ResourceManagerImplTest {

    private static final String OTHER_DUMMY_AGENT = DUMMY_AGENT + "-other";
    private static final String OTHER_HOST = "other";

    private ResourceManagerImpl resourceManager;
    private AgentRegistry agentRegistry;
    private OrchestratorServer orchestratorServer;
    private ActorConfig defaultActorConfig;

    @Before
    public void before() {
        agentRegistry = new AgentRegistry();
        orchestratorServer = mock(OrchestratorServer.class);
        resourceManager = new ResourceManagerImpl(orchestratorServer, agentRegistry);

        List<String> preferredHosts = Collections.emptyList();
        defaultActorConfig = new ActorConfig(DUMMY_ACTOR, "", new ActorConfig.DeployConfig(false, preferredHosts));
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
        preferredHosts.add(regMsgAgentOnPHost.getSystemInfo().getHostName());

        ActorConfig actorConfig = new ActorConfig(DUMMY_ACTOR, "", new ActorConfig.DeployConfig(false, preferredHosts));
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

        ActorConfig actorConfig = new ActorConfig(DUMMY_ACTOR, "", new ActorConfig.DeployConfig(false, preferredHosts));
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

        ActorConfig actorConfig = new ActorConfig(DUMMY_ACTOR, "", new ActorConfig.DeployConfig(false, preferredHosts));
        resourceManager.createActor(actorConfig);

        assertThat(resourceManager.getActorsToAgents().size(), is(1));
        RegisteredAgent pickedAgent = resourceManager.getActorsToAgents().get(DUMMY_ACTOR);
        assertNotNull(pickedAgent);
    }

    private AgentRegistrationMessage registerAgentOnOurHost() {
        AgentRegistrationMessage regMsgAgentOnPHost = AgentRegistrationMessage.create(DUMMY_AGENT);
        agentRegistry.getListenerForOrchestrator().onAgentRegistration(regMsgAgentOnPHost);
        return regMsgAgentOnPHost;
    }

    private AgentRegistrationMessage registerDummyAgentOnOtherHost(final String agentName) {
        AgentRegistrationMessage regMsgAgentOnOtherHost = spy(AgentRegistrationMessage.create(agentName));
        SystemInfo sysInfoOtherHost = mock(SystemInfo.class);
        when(sysInfoOtherHost.getHostName()).thenReturn(OTHER_HOST);
        when(regMsgAgentOnOtherHost.getSystemInfo()).thenReturn(sysInfoOtherHost);
        agentRegistry.getListenerForOrchestrator().onAgentRegistration(regMsgAgentOnOtherHost);
        return regMsgAgentOnOtherHost;
    }

}