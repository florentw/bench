package io.amaze.bench.orchestrator.registry;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static io.amaze.bench.client.runtime.agent.AgentTest.DUMMY_AGENT;
import static org.mockito.Mockito.*;

/**
 * Created on 4/2/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class AgentRegistryTest {

    private static final AgentRegistrationMessage REG_MSG = AgentRegistrationMessage.create(DUMMY_AGENT);

    private AgentRegistry registry;
    private AgentRegistryListener orchestratorListener;
    private AgentRegistryListener clientListener;

    @Before
    public void before() {
        registry = new AgentRegistry();
        orchestratorListener = registry.getListenerForOrchestrator();

        clientListener = mock(AgentRegistryListener.class);
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
    public void agent_registered() {
        orchestratorListener.onAgentRegistration(REG_MSG);

        RegisteredAgent agent = registry.byName(DUMMY_AGENT);
        assertThat(agent.getName()).is(DUMMY_AGENT);
        assertThat(agent.getSystemInfo()).is(REG_MSG.getSystemInfo());
        assertThat(agent.getCreationTime()).is(REG_MSG.getCreationTime());

        verify(clientListener).onAgentRegistration(REG_MSG);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void agent_registered_signs_off() {
        orchestratorListener.onAgentRegistration(REG_MSG);
        orchestratorListener.onAgentSignOff(DUMMY_AGENT);

        RegisteredAgent agent = registry.byName(DUMMY_AGENT);
        assertThat(agent).isNull();

        verify(clientListener).onAgentRegistration(REG_MSG);
        verify(clientListener).onAgentSignOff(DUMMY_AGENT);
        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void unknown_agent_signs_off() {
        orchestratorListener.onAgentSignOff(DUMMY_AGENT);

        RegisteredAgent agent = registry.byName(DUMMY_AGENT);
        assertThat(agent).isNull();

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void list_all() {
        orchestratorListener.onAgentRegistration(REG_MSG);

        Set<RegisteredAgent> agents = registry.all();

        assertThat(agents.size()).is(1);

        RegisteredAgent agent = agents.iterator().next();
        assertThat(agent.getName()).is(DUMMY_AGENT);
    }

    @Test
    public void removed_listener_is_not_notified() {
        registry.removeListener(clientListener);

        orchestratorListener.onAgentRegistration(REG_MSG);

        verifyNoMoreInteractions(clientListener);
    }

    @Test
    public void remove_listener_twice_does_not_throw() {
        registry.removeListener(clientListener);
        registry.removeListener(clientListener);
    }
}