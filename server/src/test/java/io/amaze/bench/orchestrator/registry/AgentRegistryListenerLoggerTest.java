package io.amaze.bench.orchestrator.registry;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import org.junit.Before;
import org.junit.Test;

import static io.amaze.bench.client.runtime.agent.AgentTest.DUMMY_AGENT;
import static org.mockito.Mockito.*;

/**
 * Created on 3/30/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class AgentRegistryListenerLoggerTest {

    private AgentRegistryListener delegateListener;
    private AgentRegistryListenerLogger loggerListener;

    @Before
    public void before() {
        delegateListener = mock(AgentRegistryListener.class);
        loggerListener = new AgentRegistryListenerLogger(delegateListener);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(AgentRegistryListenerLogger.class);
        tester.testAllPublicInstanceMethods(loggerListener);
    }

    @Test
    public void agent_creation() {
        AgentRegistrationMessage regMsg = AgentRegistrationMessage.create();
        loggerListener.onAgentRegistration(regMsg);

        verify(delegateListener).onAgentRegistration(regMsg);
        verifyNoMoreInteractions(delegateListener);
    }

    @Test
    public void agent_signoff() {
        loggerListener.onAgentSignOff(DUMMY_AGENT);

        verify(delegateListener).onAgentSignOff(DUMMY_AGENT);
        verifyNoMoreInteractions(delegateListener);
    }

}