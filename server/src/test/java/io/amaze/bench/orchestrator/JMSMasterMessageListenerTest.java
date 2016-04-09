package io.amaze.bench.orchestrator;

import io.amaze.bench.client.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.client.runtime.actor.ActorLifecycleMessage.Phase;
import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import io.amaze.bench.client.runtime.agent.MasterOutputMessage;
import io.amaze.bench.client.runtime.agent.MasterOutputMessage.Action;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.orchestrator.registry.ActorRegistryListener;
import io.amaze.bench.orchestrator.registry.AgentRegistryListener;
import io.amaze.bench.shared.jms.JMSHelper;
import org.junit.Before;
import org.junit.Test;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import java.io.IOException;

import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.client.runtime.agent.AgentTest.DUMMY_AGENT;
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
    public void null_parameters_invalid() {

    }

    @Test
    public void corrupted_message_does_nothing() throws IOException, ClassNotFoundException, JMSException {
        messageListener.onMessage(mock(javax.jms.Message.class));

        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void message_of_invalid_type_does_nothing() throws IOException, JMSException {
        byte[] data = JMSHelper.convertToBytes(new Message<>("", "Hello"));
        BytesMessage msg = createTestBytesMessage(data);

        messageListener.onMessage(msg);

        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void agent_registered() throws IOException, JMSException {
        AgentRegistrationMessage regMsg = AgentRegistrationMessage.create();
        MasterOutputMessage inputMsg = new MasterOutputMessage(Action.REGISTER_AGENT, regMsg);
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(agentRegistryLstnr).onAgentRegistration(regMsg);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void agent_signoff() throws IOException, JMSException {
        MasterOutputMessage inputMsg = new MasterOutputMessage(Action.UNREGISTER_AGENT, DUMMY_AGENT);
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(agentRegistryLstnr).onAgentSignOff(DUMMY_AGENT);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void actor_created() throws IOException, JMSException {
        ActorLifecycleMessage lfMsg = new ActorLifecycleMessage(DUMMY_ACTOR, DUMMY_AGENT, Phase.CREATED);
        MasterOutputMessage inputMsg = new MasterOutputMessage(Action.ACTOR_LIFECYCLE, lfMsg);
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryLstnr).onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void actor_started() throws IOException, JMSException {
        ActorLifecycleMessage lfMsg = new ActorLifecycleMessage(DUMMY_ACTOR, DUMMY_AGENT, Phase.STARTED);
        MasterOutputMessage inputMsg = new MasterOutputMessage(Action.ACTOR_LIFECYCLE, lfMsg);
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryLstnr).onActorStarted(DUMMY_ACTOR, DUMMY_AGENT);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void actor_failure() throws IOException, JMSException {
        ActorLifecycleMessage lfMsg = new ActorLifecycleMessage(DUMMY_ACTOR, DUMMY_AGENT, Phase.FAILED, null);

        MasterOutputMessage inputMsg = new MasterOutputMessage(Action.ACTOR_LIFECYCLE, lfMsg);
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryLstnr).onActorFailed(DUMMY_ACTOR, null);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void actor_closed() throws IOException, JMSException {
        ActorLifecycleMessage lfMsg = new ActorLifecycleMessage(DUMMY_ACTOR, DUMMY_AGENT, Phase.CLOSED);

        MasterOutputMessage inputMsg = new MasterOutputMessage(Action.ACTOR_LIFECYCLE, lfMsg);
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryLstnr).onActorClosed(DUMMY_ACTOR);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    private BytesMessage toBytesMessage(final MasterOutputMessage masterMsg) throws IOException, JMSException {
        Message<MasterOutputMessage> message = new Message<>("", masterMsg);
        final byte[] data = JMSHelper.convertToBytes(message);
        return createTestBytesMessage(data);
    }
}