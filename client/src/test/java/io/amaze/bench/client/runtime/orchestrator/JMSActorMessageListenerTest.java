package io.amaze.bench.client.runtime.orchestrator;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.client.runtime.actor.Actor;
import io.amaze.bench.client.runtime.actor.ActorInputMessage;
import io.amaze.bench.shared.jms.JMSHelper;
import org.junit.Before;
import org.junit.Test;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;

import static io.amaze.bench.client.runtime.actor.ActorInputMessage.Command.*;
import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.shared.jms.JMSHelperTest.createTestBytesMessage;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;

/**
 * Created on 3/19/16.
 */
public final class JMSActorMessageListenerTest {

    private static final String DUMMY_PAYLOAD = "hello";

    private Actor mockActor;
    private JMSActorMessageListener listener;

    @Before
    public void before() {
        mockActor = mock(Actor.class);
        listener = new JMSActorMessageListener(mockActor);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(JMSActorMessageListener.class);
        tester.testAllPublicInstanceMethods(listener);
    }

    @Test
    public void invalid_jms_message_does_not_throw() {
        listener.onMessage(mock(Message.class));
        verifyNoMoreInteractions(mockActor);
    }

    @Test
    public void initialize_actor_msg() throws IOException, ClassNotFoundException, JMSException {
        ActorInputMessage inputMsg = new ActorInputMessage(INIT, DUMMY_ACTOR, "");

        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        BytesMessage msg = createTestBytesMessage(data);

        listener.onMessage(msg);

        verify(mockActor).init();
        verifyNoMoreInteractions(mockActor);
    }

    @Test
    public void stop_actor_msg() throws IOException, ClassNotFoundException, JMSException {
        ActorInputMessage inputMsg = new ActorInputMessage(CLOSE, DUMMY_ACTOR, "");

        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        BytesMessage msg = createTestBytesMessage(data);

        listener.onMessage(msg);

        verify(mockActor).close();
        verifyNoMoreInteractions(mockActor);
    }

    @Test
    public void dump_actor_metrics_msg() throws IOException, ClassNotFoundException, JMSException {
        ActorInputMessage inputMsg = new ActorInputMessage(DUMP_METRICS, DUMMY_ACTOR, "");

        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        BytesMessage msg = createTestBytesMessage(data);

        listener.onMessage(msg);

        verify(mockActor).dumpAndFlushMetrics();
        verifyNoMoreInteractions(mockActor);
    }

    @Test(expected = NullPointerException.class)
    public void on_actor_null_message_msg_throws() throws IOException, ClassNotFoundException, JMSException {
        ActorInputMessage inputMsg = new ActorInputMessage(MESSAGE, DUMMY_ACTOR, null);

        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        BytesMessage msg = createTestBytesMessage(data);

        listener.onMessage(msg);
    }

    @Test
    public void on_actor_message_msg() throws IOException, ClassNotFoundException, JMSException {
        ActorInputMessage inputMsg = new ActorInputMessage(MESSAGE, DUMMY_ACTOR, DUMMY_PAYLOAD);

        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        BytesMessage msg = createTestBytesMessage(data);

        listener.onMessage(msg);

        verify(mockActor).onMessage(argThat(is(DUMMY_ACTOR)), argThat(is(DUMMY_PAYLOAD)));
        verifyNoMoreInteractions(mockActor);
    }
}