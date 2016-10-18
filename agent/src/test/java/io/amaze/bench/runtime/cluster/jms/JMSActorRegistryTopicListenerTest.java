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
package io.amaze.bench.runtime.cluster.jms;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.LifecycleMessage;
import io.amaze.bench.runtime.actor.ActorDeployInfo;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryListener;
import io.amaze.bench.runtime.message.Message;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSHelper;
import io.amaze.bench.shared.util.Network;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import java.io.IOException;

import static io.amaze.bench.runtime.actor.ActorLifecycleMessage.*;
import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.runtime.agent.AgentTest.DUMMY_AGENT;
import static io.amaze.bench.shared.jms.JMSHelperTest.createTestBytesMessage;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 9/25/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSActorRegistryTopicListenerTest {

    private final Endpoint endpoint = new JMSEndpoint(Network.LOCALHOST, 1337);

    @Mock
    private ActorRegistryListener actorRegistryListener;
    private JMSActorRegistryTopicListener messageListener;

    @Before
    public void before() {
        messageListener = new JMSActorRegistryTopicListener(actorRegistryListener);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(JMSActorRegistryTopicListener.class);
        tester.testAllPublicInstanceMethods(messageListener);
    }

    @Test
    public void corrupted_message_does_nothing() throws IOException, ClassNotFoundException, JMSException {
        messageListener.onMessage(mock(javax.jms.Message.class));

        verifyNoMoreInteractions(actorRegistryListener);
    }

    @Test
    public void message_of_invalid_type_does_nothing() throws IOException, JMSException {
        byte[] data = JMSHelper.convertToBytes(new Message<>("", "Hello"));
        BytesMessage msg = createTestBytesMessage(data);

        messageListener.onMessage(msg);

        verifyNoMoreInteractions(actorRegistryListener);
    }

    @Test
    public void actor_created() throws IOException, JMSException {
        BytesMessage msg = toBytesMessage(created(DUMMY_ACTOR, DUMMY_AGENT));

        messageListener.onMessage(msg);

        verify(actorRegistryListener).onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        verifyNoMoreInteractions(actorRegistryListener);
    }

    @Test
    public void actor_initialized() throws IOException, JMSException {
        ActorDeployInfo deployInfo = new ActorDeployInfo(endpoint, 10);
        BytesMessage msg = toBytesMessage(initialized(DUMMY_ACTOR, deployInfo));

        messageListener.onMessage(msg);

        verify(actorRegistryListener).onActorInitialized(DUMMY_ACTOR, deployInfo);
        verifyNoMoreInteractions(actorRegistryListener);
    }

    @Test
    public void actor_failure() throws IOException, JMSException {
        Throwable throwable = new IllegalArgumentException();
        BytesMessage msg = toBytesMessage(failed(DUMMY_ACTOR, throwable));

        messageListener.onMessage(msg);

        verify(actorRegistryListener).onActorFailed(eq(DUMMY_ACTOR), any(Throwable.class));
        verifyNoMoreInteractions(actorRegistryListener);
    }

    @Test
    public void actor_closed() throws IOException, JMSException {
        BytesMessage msg = toBytesMessage(closed(DUMMY_ACTOR));

        messageListener.onMessage(msg);

        verify(actorRegistryListener).onActorClosed(DUMMY_ACTOR);
        verifyNoMoreInteractions(actorRegistryListener);
    }

    private BytesMessage toBytesMessage(final LifecycleMessage lifecycleMessage) throws IOException, JMSException {
        Message<LifecycleMessage> message = new Message<>("", lifecycleMessage);
        final byte[] data = JMSHelper.convertToBytes(message);
        return createTestBytesMessage(data);
    }
}