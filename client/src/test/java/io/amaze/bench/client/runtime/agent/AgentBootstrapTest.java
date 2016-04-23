package io.amaze.bench.client.runtime.agent;

import io.amaze.bench.client.runtime.orchestrator.OrchestratorActor;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorAgent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created on 4/10/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class AgentBootstrapTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private DummyClientFactory clientFactory;

    @Before
    public void before() {
        clientFactory = new DummyClientFactory(mock(OrchestratorAgent.class), mock(OrchestratorActor.class));
    }

    @Test
    public void invalid_arguments_show_usage() {
        AgentBootstrap.main(new String[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalid_arguments_throw() {
        AgentBootstrap.main(new String[]{"dummy", "not_an_int"});
    }

    @Test
    public void install_shutdown_hook() {
        Agent agent = AgentBootstrap.createAgent(clientFactory);

        Thread shutdownHook = AgentBootstrap.registerShutdownHook(agent);

        assertNotNull(shutdownHook);
        boolean removed = Runtime.getRuntime().removeShutdownHook(shutdownHook);
        assertThat(removed, is(true));
    }

    @Test
    public void shutdown_hook_closes_agent() throws Exception {
        Agent agent = spy(AgentBootstrap.createAgent(clientFactory));
        Thread shutdownHook = AgentBootstrap.registerShutdownHook(agent);

        shutdownHook.run();

        verify(agent).close();
    }

    @Test
    public void shutdown_hook_closes_agent_and_close_fail_does_nothing() throws Exception {
        Agent agent = spy(AgentBootstrap.createAgent(clientFactory));
        doThrow(new IllegalArgumentException()).when(agent).close();
        Thread shutdownHook = AgentBootstrap.registerShutdownHook(agent);

        shutdownHook.run();

        verify(agent).close();
    }

    @Test
    public void can_create_agent() {
        Agent agent = AgentBootstrap.createAgent(clientFactory);

        assertNotNull(agent);
        assertNotNull(agent.getName());
    }

}