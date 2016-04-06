package io.amaze.bench.client.runtime.orchestrator;

import io.amaze.bench.client.runtime.actor.TestActor;
import io.amaze.bench.client.runtime.agent.AgentClientListener;
import io.amaze.bench.client.runtime.agent.AgentInputMessage;
import io.amaze.bench.client.runtime.agent.AgentInputMessage.Action;
import io.amaze.bench.shared.jms.JMSHelper;
import org.junit.Before;
import org.junit.Test;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;

import static io.amaze.bench.client.runtime.agent.AgentTest.DUMMY_AGENT;
import static io.amaze.bench.shared.jms.JMSHelperTest.createTestBytesMessage;
import static org.mockito.Mockito.*;

/**
 * Created on 3/19/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class JMSAgentMessageListenerTest {

    private AgentClientListener agentListener;
    private JMSAgentMessageListener listener;

    @Before
    public void before() {
        agentListener = mock(AgentClientListener.class);
        listener = new JMSAgentMessageListener(DUMMY_AGENT, agentListener);
    }

    @Test
    public void create_actor_calls_listener() throws IOException, ClassNotFoundException, JMSException {
        ActorCreationRequest creationRequest = new ActorCreationRequest(TestActor.DUMMY_CONFIG);

        AgentInputMessage masterMsg = new AgentInputMessage(DUMMY_AGENT, Action.CREATE_ACTOR, creationRequest);
        BytesMessage testBytesMessage = toBytesMessage(masterMsg);

        listener.onMessage(testBytesMessage);

        verify(agentListener).onActorCreationRequest(eq(TestActor.DUMMY_CONFIG));
        verifyNoMoreInteractions(agentListener);
    }

    @Test
    public void create_actor_for_wrong_agent_does_nothing() throws IOException, ClassNotFoundException, JMSException {
        ActorCreationRequest creationRequest = new ActorCreationRequest(TestActor.DUMMY_CONFIG);

        AgentInputMessage masterMsg = new AgentInputMessage(DUMMY_AGENT + "2", Action.CREATE_ACTOR, creationRequest);
        BytesMessage testBytesMessage = toBytesMessage(masterMsg);

        listener.onMessage(testBytesMessage);

        verifyNoMoreInteractions(agentListener);
    }

    @Test
    public void close_actor_calls_listener() throws IOException, ClassNotFoundException, JMSException {
        AgentInputMessage masterMsg = new AgentInputMessage(DUMMY_AGENT, Action.CLOSE_ACTOR, TestActor.DUMMY_ACTOR);
        BytesMessage testBytesMessage = toBytesMessage(masterMsg);

        listener.onMessage(testBytesMessage);

        verify(agentListener).onActorCloseRequest(TestActor.DUMMY_ACTOR);
        verifyNoMoreInteractions(agentListener);
    }

    @Test
    public void error_on_message_does_not_throw() throws IOException, ClassNotFoundException, JMSException {
        listener.onMessage(mock(Message.class));

        verifyNoMoreInteractions(agentListener);
    }

    private BytesMessage toBytesMessage(final AgentInputMessage masterMsg) throws IOException, ClassNotFoundException, JMSException {
        byte[] bytes = JMSHelper.convertToBytes(masterMsg);
        return createTestBytesMessage(bytes);
    }
}