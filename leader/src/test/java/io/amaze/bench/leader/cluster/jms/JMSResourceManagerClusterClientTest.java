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
package io.amaze.bench.leader.cluster.jms;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.runtime.agent.AgentInputMessage;
import io.amaze.bench.runtime.cluster.ActorCreationRequest;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.runtime.actor.TestActor.DUMMY_CONFIG;
import static io.amaze.bench.runtime.agent.AgentTest.DUMMY_AGENT;
import static io.amaze.bench.runtime.agent.Constants.AGENTS_TOPIC;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Created on 4/3/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSResourceManagerClusterClientTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private JMSResourceManagerClusterClient rmClusterClient;
    @Mock
    private JMSServer jmsServer;
    @Mock
    private JMSClient jmsClient;

    @Before
    public void before() {
        rmClusterClient = new JMSResourceManagerClusterClient(jmsServer, jmsClient);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(JMSResourceManagerClusterClient.class);
        tester.testAllPublicInstanceMethods(rmClusterClient);
    }

    @Test
    public void create_actor_queue() throws JMSException {
        rmClusterClient.initForActor(DUMMY_ACTOR);

        verify(jmsServer).createQueue(DUMMY_ACTOR.getName());
    }

    @Test
    public void create_actor_queue_fails_and_rethrows() throws JMSException {
        JMSException expectedCause = new JMSException(null);
        doThrow(expectedCause).when(jmsServer).createQueue(any(String.class));

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(expectedCause));

        rmClusterClient.initForActor(DUMMY_ACTOR);
    }

    @Test
    public void delete_actor_queue() throws JMSException {
        rmClusterClient.closeForActor(DUMMY_ACTOR);

        verify(jmsServer).deleteQueue(DUMMY_ACTOR.getName());
    }

    @Test
    public void send_to_agent() throws JMSException {
        AgentInputMessage msg = AgentInputMessage.createActor(DUMMY_AGENT, new ActorCreationRequest(DUMMY_CONFIG));
        rmClusterClient.sendToAgent(msg);

        verify(jmsClient).sendToTopic(AGENTS_TOPIC, msg);
    }

    @Test
    public void send_to_agent_fails_and_rethrows() throws JMSException {
        JMSException expectedCause = new JMSException(null);
        doThrow(expectedCause).when(jmsClient).sendToTopic(any(String.class), any(Serializable.class));

        AgentInputMessage msg = AgentInputMessage.createActor(DUMMY_AGENT, new ActorCreationRequest(DUMMY_CONFIG));

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(expectedCause));

        rmClusterClient.sendToAgent(msg);
    }

}