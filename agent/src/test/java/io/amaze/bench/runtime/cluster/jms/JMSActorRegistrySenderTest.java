package io.amaze.bench.runtime.cluster.jms;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.runtime.agent.Constants.ACTOR_REGISTRY_TOPIC;
import static io.amaze.bench.util.Matchers.isActorLifecycle;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created on 10/19/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSActorRegistrySenderTest {

    private static final String TEST_AGENT = "agent";

    @Mock
    private JMSClient client;
    private JMSActorRegistrySender sender;

    @Before
    public void init() {
        sender = new JMSActorRegistrySender(client, TEST_AGENT);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(JMSActorRegistrySender.class);
        tester.testAllPublicInstanceMethods(sender);
    }

    @Test
    public void send_to_actor_registry_sends_to_topic() throws JMSException {
        ActorLifecycleMessage closed = ActorLifecycleMessage.closed(TestActor.DUMMY_ACTOR);
        sender.send(closed);

        verify(client).sendToTopic(eq(ACTOR_REGISTRY_TOPIC), argThat(isActorLifecycle(TEST_AGENT, closed)));
        verifyNoMoreInteractions(client);
    }

}