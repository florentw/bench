/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
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
import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.actor.ActorInputMessage;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created on 9/16/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSActorSenderTest {

    private static final ActorInputMessage DUMMY_MSG = ActorInputMessage.dumpMetrics();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private JMSClient jmsClient;
    private JMSActorSender sender;

    @Before
    public void initSender() {
        sender = new JMSActorSender(jmsClient);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorInputMessage.class, DUMMY_MSG);
        tester.setDefault(ActorKey.class, DUMMY_ACTOR);

        tester.testAllPublicConstructors(JMSActorSender.class);
        tester.testAllPublicInstanceMethods(sender);
    }

    @Test
    public void send_to_actor() throws JMSException {
        sender.send(DUMMY_ACTOR, DUMMY_MSG);

        verify(jmsClient).sendToQueue(DUMMY_ACTOR.getName(), DUMMY_MSG);
        verifyNoMoreInteractions(jmsClient);
    }

    @Test
    public void send_to_actor_fails_and_rethrows() throws JMSException {
        JMSException expectedCause = new JMSException(null);

        doThrow(expectedCause).when(jmsClient).sendToQueue(any(String.class), any(Serializable.class));
        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(expectedCause));

        sender.send(DUMMY_ACTOR, DUMMY_MSG);
    }

}