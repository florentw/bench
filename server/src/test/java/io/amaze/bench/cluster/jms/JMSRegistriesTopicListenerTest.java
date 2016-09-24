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
package io.amaze.bench.cluster.jms;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.client.runtime.actor.ActorDeployInfo;
import io.amaze.bench.client.runtime.agent.AgentOutputMessage;
import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.cluster.registry.ActorRegistryListener;
import io.amaze.bench.cluster.registry.AgentRegistryListener;
import io.amaze.bench.shared.jms.JMSHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import java.io.IOException;

import static io.amaze.bench.client.runtime.actor.ActorLifecycleMessage.*;
import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.client.runtime.agent.AgentOutputMessage.Action.*;
import static io.amaze.bench.client.runtime.agent.AgentTest.DUMMY_AGENT;
import static io.amaze.bench.shared.jms.JMSHelperTest.createTestBytesMessage;
import static org.mockito.Mockito.*;

/**
 * Created on 3/29/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSRegistriesTopicListenerTest {

    @Mock
    private AgentRegistryListener agentRegistryListener;
    @Mock
    private ActorRegistryListener actorRegistryListener;
    private JMSRegistriesTopicListener messageListener;

    @Before
    public void before() {
        messageListener = new JMSRegistriesTopicListener(agentRegistryListener, actorRegistryListener);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(JMSRegistriesTopicListener.class);
        tester.testAllPublicInstanceMethods(messageListener);
    }

    @Test
    public void corrupted_message_does_nothing() throws IOException, ClassNotFoundException, JMSException {
        messageListener.onMessage(mock(javax.jms.Message.class));

        verifyNoMoreInteractions(agentRegistryListener);
        verifyNoMoreInteractions(actorRegistryListener);
    }

    @Test
    public void message_of_invalid_type_does_nothing() throws IOException, JMSException {
        byte[] data = JMSHelper.convertToBytes(new Message<>("", "Hello"));
        BytesMessage msg = createTestBytesMessage(data);

        messageListener.onMessage(msg);

        verifyNoMoreInteractions(agentRegistryListener);
        verifyNoMoreInteractions(actorRegistryListener);
    }

    @Test
    public void agent_registered() throws IOException, JMSException {
        AgentRegistrationMessage regMsg = AgentRegistrationMessage.create("dummy-agent");
        AgentOutputMessage inputMsg = new AgentOutputMessage(REGISTER_AGENT, regMsg);
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(agentRegistryListener).onAgentRegistration(regMsg);
        verifyNoMoreInteractions(agentRegistryListener);
        verifyNoMoreInteractions(actorRegistryListener);
    }

    @Test
    public void agent_signoff() throws IOException, JMSException {
        AgentOutputMessage inputMsg = new AgentOutputMessage(UNREGISTER_AGENT, DUMMY_AGENT);
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(agentRegistryListener).onAgentSignOff(DUMMY_AGENT);
        verifyNoMoreInteractions(agentRegistryListener);
        verifyNoMoreInteractions(actorRegistryListener);
    }

    @Test
    public void actor_created() throws IOException, JMSException {
        AgentOutputMessage inputMsg = new AgentOutputMessage(ACTOR_LIFECYCLE, created(DUMMY_ACTOR, DUMMY_AGENT));
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryListener).onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        verifyNoMoreInteractions(agentRegistryListener);
        verifyNoMoreInteractions(actorRegistryListener);
    }

    @Test
    public void actor_initialized() throws IOException, JMSException {
        ActorDeployInfo deployInfo = new ActorDeployInfo(10);
        AgentOutputMessage inputMsg = new AgentOutputMessage(ACTOR_LIFECYCLE, initialized(DUMMY_ACTOR, deployInfo));
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryListener).onActorInitialized(DUMMY_ACTOR, deployInfo);
        verifyNoMoreInteractions(agentRegistryListener);
        verifyNoMoreInteractions(actorRegistryListener);
    }

    @Test
    public void actor_failure() throws IOException, JMSException {
        Throwable throwable = new IllegalArgumentException();
        AgentOutputMessage inputMsg = new AgentOutputMessage(ACTOR_LIFECYCLE, failed(DUMMY_ACTOR, throwable));
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryListener).onActorFailed(eq(DUMMY_ACTOR), any(Throwable.class));
        verifyNoMoreInteractions(agentRegistryListener);
        verifyNoMoreInteractions(actorRegistryListener);
    }

    @Test
    public void actor_closed() throws IOException, JMSException {
        AgentOutputMessage inputMsg = new AgentOutputMessage(ACTOR_LIFECYCLE, closed(DUMMY_ACTOR));
        BytesMessage msg = toBytesMessage(inputMsg);

        messageListener.onMessage(msg);

        verify(actorRegistryListener).onActorClosed(DUMMY_ACTOR);
        verifyNoMoreInteractions(agentRegistryListener);
        verifyNoMoreInteractions(actorRegistryListener);
    }

    private BytesMessage toBytesMessage(final AgentOutputMessage masterMsg) throws IOException, JMSException {
        Message<AgentOutputMessage> message = new Message<>("", masterMsg);
        final byte[] data = JMSHelper.convertToBytes(message);
        return createTestBytesMessage(data);
    }
}