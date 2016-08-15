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
import io.amaze.bench.client.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.client.runtime.actor.ActorLifecycleMessage.Phase;
import io.amaze.bench.client.runtime.agent.AgentOutputMessage;
import io.amaze.bench.client.runtime.agent.AgentOutputMessage.Action;
import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
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
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(JMSMasterMessageListener.class);
        tester.testAllPublicInstanceMethods(messageListener);
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
        AgentOutputMessage inputMsg = new AgentOutputMessage(Action.REGISTER_AGENT, regMsg);
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(agentRegistryLstnr).onAgentRegistration(regMsg);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void agent_signoff() throws IOException, JMSException {
        AgentOutputMessage inputMsg = new AgentOutputMessage(Action.UNREGISTER_AGENT, DUMMY_AGENT);
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(agentRegistryLstnr).onAgentSignOff(DUMMY_AGENT);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void actor_created() throws IOException, JMSException {
        ActorLifecycleMessage lfMsg = new ActorLifecycleMessage(DUMMY_ACTOR, DUMMY_AGENT, Phase.CREATED);
        AgentOutputMessage inputMsg = new AgentOutputMessage(Action.ACTOR_LIFECYCLE, lfMsg);
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryLstnr).onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void actor_initialized() throws IOException, JMSException {
        ActorLifecycleMessage lfMsg = new ActorLifecycleMessage(DUMMY_ACTOR, DUMMY_AGENT, Phase.INITIALIZED);
        AgentOutputMessage inputMsg = new AgentOutputMessage(Action.ACTOR_LIFECYCLE, lfMsg);
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryLstnr).onActorInitialized(DUMMY_ACTOR, DUMMY_AGENT);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void actor_failure() throws IOException, JMSException {
        ActorLifecycleMessage lfMsg = new ActorLifecycleMessage(DUMMY_ACTOR, DUMMY_AGENT, Phase.FAILED, null);

        AgentOutputMessage inputMsg = new AgentOutputMessage(Action.ACTOR_LIFECYCLE, lfMsg);
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryLstnr).onActorFailed(DUMMY_ACTOR, null);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    @Test
    public void actor_closed() throws IOException, JMSException {
        ActorLifecycleMessage lfMsg = new ActorLifecycleMessage(DUMMY_ACTOR, DUMMY_AGENT, Phase.CLOSED);

        AgentOutputMessage inputMsg = new AgentOutputMessage(Action.ACTOR_LIFECYCLE, lfMsg);
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryLstnr).onActorClosed(DUMMY_ACTOR);
        verifyNoMoreInteractions(agentRegistryLstnr);
        verifyNoMoreInteractions(actorRegistryLstnr);
    }

    private BytesMessage toBytesMessage(final AgentOutputMessage masterMsg) throws IOException, JMSException {
        Message<AgentOutputMessage> message = new Message<>("", masterMsg);
        final byte[] data = JMSHelper.convertToBytes(message);
        return createTestBytesMessage(data);
    }
}