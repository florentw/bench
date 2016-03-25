package io.amaze.bench.client.runtime.orchestrator;

import io.amaze.bench.client.runtime.actor.Actor;
import io.amaze.bench.client.runtime.actor.ActorInputMessage;
import io.amaze.bench.client.runtime.actor.TestActor;
import io.amaze.bench.shared.jms.JMSHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created on 3/19/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class JMSActorMessageListenerTest {

    private static final String DUMMY_PAYLOAD = "hello";

    private Actor mockActor;
    private JMSActorMessageListener msgListener;

    static BytesMessage createTestBytesMessage(final byte[] data) throws JMSException {
        BytesMessage msg = mock(BytesMessage.class);
        when(msg.getBodyLength()).thenReturn((long) data.length);
        when(msg.readBytes(any(byte[].class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                byte[] out = (byte[]) invocation.getArguments()[0];
                System.arraycopy(data, 0, out, 0, out.length);
                return null;
            }
        }).thenReturn(data.length);
        return msg;
    }

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
        ActorInputMessage inputMsg = new ActorInputMessage(ActorInputMessage.Command.START,
                                                           TestActor.DUMMY_ACTOR,
                                                           null);

        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        BytesMessage msg = createTestBytesMessage(data);

        msgListener.onMessage(msg);

        verify(mockActor).start();
        verifyNoMoreInteractions(mockActor);
    }

    @Test
    public void stop_actor_msg() throws IOException, ClassNotFoundException, JMSException {
        ActorInputMessage inputMsg = new ActorInputMessage(ActorInputMessage.Command.STOP, TestActor.DUMMY_ACTOR, null);

        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        BytesMessage msg = createTestBytesMessage(data);

        msgListener.onMessage(msg);

        verify(mockActor).close();
        verifyNoMoreInteractions(mockActor);
    }

    @Test
    public void dump_actor_metrics_msg() throws IOException, ClassNotFoundException, JMSException {
        ActorInputMessage inputMsg = new ActorInputMessage(ActorInputMessage.Command.DUMP_METRICS,
                                                           TestActor.DUMMY_ACTOR,
                                                           null);

        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        BytesMessage msg = createTestBytesMessage(data);

        msgListener.onMessage(msg);

        verify(mockActor).dumpMetrics();
        verifyNoMoreInteractions(mockActor);
    }

    @Test(expected = NullPointerException.class)
    public void on_actor_null_message_msg_throws() throws IOException, ClassNotFoundException, JMSException {
        ActorInputMessage inputMsg = new ActorInputMessage(ActorInputMessage.Command.MESSAGE,
                                                           TestActor.DUMMY_ACTOR,
                                                           null);

        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        BytesMessage msg = createTestBytesMessage(data);

        msgListener.onMessage(msg);
    }

    @Test
    public void on_actor_message_msg() throws IOException, ClassNotFoundException, JMSException {
        ActorInputMessage inputMsg = new ActorInputMessage(ActorInputMessage.Command.MESSAGE,
                                                           TestActor.DUMMY_ACTOR,
                                                           DUMMY_PAYLOAD);

        final byte[] data = JMSHelper.convertToBytes(inputMsg);
        BytesMessage msg = createTestBytesMessage(data);

        msgListener.onMessage(msg);

        verify(mockActor).onMessage(argThat(is(TestActor.DUMMY_ACTOR)), argThat(is(DUMMY_PAYLOAD)));
        verifyNoMoreInteractions(mockActor);
    }
}