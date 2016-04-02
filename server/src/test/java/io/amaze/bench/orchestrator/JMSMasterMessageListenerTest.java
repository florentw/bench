package io.amaze.bench.orchestrator;

import io.amaze.bench.TestConstants;
import io.amaze.bench.client.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.client.runtime.actor.ActorLifecycleMessage.Phase;
import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import io.amaze.bench.client.runtime.agent.MasterOutputMessage;
import io.amaze.bench.client.runtime.agent.MasterOutputMessage.Action;
import io.amaze.bench.orchestrator.registry.ActorRegistryListener;
import io.amaze.bench.orchestrator.registry.AgentRegistryListener;
import io.amaze.bench.shared.jms.JMSHelper;
import org.junit.Before;
import org.junit.Test;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;

import static io.amaze.bench.shared.jms.JMSHelperTest.createTestBytesMessage;
import static org.mockito.Mockito.*;

/**
 * Created on 3/29/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class JMSMasterMessageListenerTest {

    private AgentRegistryListener agentRegistryLstnr;
    private ActorRegistryListener actorRegistryLstnr;
    private JMSMasterMessageListener messageListener;

    @Before
    public void before() {
        agentRegistryLstnr = mock(AgentRegistryListener.class);
        actorRegistryLstnr = mock(ActorRegistryListener.class);

        messageListener = new JMSMasterMessageListener(agentRegistryLstnr, actorRegistryLstnr);
    }

    @Test
    public void corrupted_message_does_nothing() throws IOException, ClassNotFoundException, JMSException {
        messageListener.onMessage(mock(Message.class));

        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void agent_registered() throws IOException, JMSException {
        AgentRegistrationMessage regMsg = AgentRegistrationMessage.create();
        MasterOutputMessage inputMsg = new MasterOutputMessage(Action.REGISTER_AGENT, regMsg);
        BytesMessage msg = bytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(agentRegistryLstnr).onAgentRegistration(regMsg);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void agent_signoff() throws IOException, JMSException {
        MasterOutputMessage inputMsg = new MasterOutputMessage(Action.UNREGISTER_AGENT, TestConstants.DUMMY_AGENT);
        BytesMessage msg = bytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(agentRegistryLstnr).onAgentSignOff(TestConstants.DUMMY_AGENT);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void actor_created() throws IOException, JMSException {
        ActorLifecycleMessage lfMsg = new ActorLifecycleMessage(TestConstants.DUMMY_ACTOR,
                                                                TestConstants.DUMMY_AGENT,
                                                                Phase.CREATED);
        MasterOutputMessage inputMsg = new MasterOutputMessage(Action.ACTOR_LIFECYCLE, lfMsg);
        BytesMessage msg = bytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryLstnr).onActorCreated(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void actor_started() throws IOException, JMSException {
        ActorLifecycleMessage lfMsg = new ActorLifecycleMessage(TestConstants.DUMMY_ACTOR,
                                                                TestConstants.DUMMY_AGENT,
                                                                Phase.STARTED);
        MasterOutputMessage inputMsg = new MasterOutputMessage(Action.ACTOR_LIFECYCLE, lfMsg);
        BytesMessage msg = bytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryLstnr).onActorStarted(TestConstants.DUMMY_ACTOR, TestConstants.DUMMY_AGENT);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void actor_failure() throws IOException, JMSException {
        ActorLifecycleMessage lfMsg = new ActorLifecycleMessage(TestConstants.DUMMY_ACTOR,
                                                                TestConstants.DUMMY_AGENT,
                                                                Phase.FAILED,
                                                                null);

        MasterOutputMessage inputMsg = new MasterOutputMessage(Action.ACTOR_LIFECYCLE, lfMsg);
        BytesMessage msg = bytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryLstnr).onActorFailed(TestConstants.DUMMY_ACTOR, null);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void actor_closed() throws IOException, JMSException {
        ActorLifecycleMessage lfMsg = new ActorLifecycleMessage(TestConstants.DUMMY_ACTOR,
                                                                TestConstants.DUMMY_AGENT,
                                                                Phase.CLOSED);

        MasterOutputMessage inputMsg = new MasterOutputMessage(Action.ACTOR_LIFECYCLE, lfMsg);
        BytesMessage msg = bytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryLstnr).onActorClosed(TestConstants.DUMMY_ACTOR);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    private BytesMessage bytesMessage(final MasterOutputMessage inputMsg) throws IOException, JMSException {
        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        return createTestBytesMessage(data);
    }
}