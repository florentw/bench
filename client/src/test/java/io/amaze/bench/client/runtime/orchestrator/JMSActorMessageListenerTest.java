package io.amaze.bench.client.runtime.orchestrator;

import io.amaze.bench.client.runtime.actor.Actor;
import io.amaze.bench.client.runtime.actor.ActorInputMessage;
import io.amaze.bench.client.runtime.actor.TestActor;
import io.amaze.bench.shared.jms.JMSHelper;
import org.junit.Before;
import org.junit.Test;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;

import static io.amaze.bench.client.runtime.actor.ActorInputMessage.Command;
import static io.amaze.bench.shared.jms.JMSHelperTest.createTestBytesMessage;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;

/**
 * Created on 3/19/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class JMSActorMessageListenerTest {

    private static final String DUMMY_PAYLOAD = "hello";

    private Actor mockActor;
    private JMSActorMessageListener msgListener;

    @Before
    public void before() {
        mockActor = mock(Actor.class);
        msgListener = new JMSActorMessageListener(mockActor);
    }

    @Test
    public void null_jms_message_does_not_throw() {
        msgListener.onMessage(null);
        verifyNoMoreInteractions(mockActor);
    }

    @Test
    public void invalid_jms_message_does_not_throw() {
        msgListener.onMessage(mock(Message.class));
        verifyNoMoreInteractions(mockActor);
    }

    @Test
    public void start_actor_msg() throws IOException, ClassNotFoundException, JMSException {
        ActorInputMessage inputMsg = new ActorInputMessage(Command.START,
                                                           TestActor.DUMMY_ACTOR, "");

        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        BytesMessage msg = createTestBytesMessage(data);

        msgListener.onMessage(msg);

        verify(mockActor).start();
        verifyNoMoreInteractions(mockActor);
    }

    @Test
    public void stop_actor_msg() throws IOException, ClassNotFoundException, JMSException {
        ActorInputMessage inputMsg = new ActorInputMessage(Command.STOP, TestActor.DUMMY_ACTOR, "");

        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        BytesMessage msg = createTestBytesMessage(data);

        msgListener.onMessage(msg);

        verify(mockActor).close();
        verifyNoMoreInteractions(mockActor);
    }

    @Test
    public void dump_actor_metrics_msg() throws IOException, ClassNotFoundException, JMSException {
        ActorInputMessage inputMsg = new ActorInputMessage(Command.DUMP_METRICS,
                                                           TestActor.DUMMY_ACTOR, "");

        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        BytesMessage msg = createTestBytesMessage(data);

        msgListener.onMessage(msg);

        verify(mockActor).dumpMetrics();
        verifyNoMoreInteractions(mockActor);
    }

    @Test(expected = NullPointerException.class)
    public void on_actor_null_message_msg_throws() throws IOException, ClassNotFoundException, JMSException {
        ActorInputMessage inputMsg = new ActorInputMessage(Command.MESSAGE,
                                                           TestActor.DUMMY_ACTOR,
                                                           null);

        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        BytesMessage msg = createTestBytesMessage(data);

        msgListener.onMessage(msg);
    }

    @Test
    public void on_actor_message_msg() throws IOException, ClassNotFoundException, JMSException {
        ActorInputMessage inputMsg = new ActorInputMessage(Command.MESSAGE,
                                                           TestActor.DUMMY_ACTOR,
                                                           DUMMY_PAYLOAD);

        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        BytesMessage msg = createTestBytesMessage(data);

        msgListener.onMessage(msg);

        verify(mockActor).onMessage(argThat(is(TestActor.DUMMY_ACTOR)), argThat(is(DUMMY_PAYLOAD)));
        verifyNoMoreInteractions(mockActor);
    }
}