package io.amaze.bench.orchestrator.registry;

import io.amaze.bench.TestConstants;
import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Created on 3/30/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class AgentRegistryListenerLoggerTest {

    private static final String DUMMY_AGENT = TestConstants.DUMMY_AGENT;

    private AgentRegistryListener delegateListener;
    private AgentRegistryListenerLogger listenerLogger;

    @Before
    public void before() {
        delegateListener = mock(AgentRegistryListener.class);
        listenerLogger = new AgentRegistryListenerLogger(delegateListener);
    }

    @Test
    public void agent_creation() {
        AgentRegistrationMessage regMsg = AgentRegistrationMessage.create();
        listenerLogger.onAgentRegistration(regMsg);

        verify(delegateListener).onAgentRegistration(regMsg);
        verifyNoMoreInteractions(delegateListener);
    }

    @Test
    public void agent_signoff() {
        listenerLogger.onAgentSignOff(DUMMY_AGENT);

        verify(delegateListener).onAgentSignOff(DUMMY_AGENT);
        verifyNoMoreInteractions(delegateListener);
    }

}