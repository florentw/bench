package io.amaze.bench.client.runtime.orchestrator;

import io.amaze.bench.client.runtime.actor.Actor;
import io.amaze.bench.client.runtime.actor.TestActor;
import io.amaze.bench.client.runtime.agent.AgentClientListener;
import io.amaze.bench.client.runtime.agent.Constants;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import org.junit.Before;
import org.junit.Test;

import javax.jms.MessageListener;
import java.io.Serializable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 3/27/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class JMSOrchestratorClientTest {

    private static final String TEST_AGENT = "agent1";

    private JMSClient jmsClient;
    private JMSOrchestratorClient client;

    @Before
    public void before() {
        jmsClient = mock(JMSClient.class);
        client = new JMSOrchestratorClient(jmsClient);
    }

    @Test
    public void start_agent_listener() throws JMSException {
        client.startAgentListener(TEST_AGENT, Constants.AGENTS_ACTOR_NAME, mock(AgentClientListener.class));

        verify(jmsClient).addTopicListener(eq(Constants.AGENTS_ACTOR_NAME), any(MessageListener.class));
        verify(jmsClient).startListening();
    }

    @Test
    public void start_actor_listener() throws JMSException {
        Actor actor = mock(Actor.class);
        when(actor.name()).thenReturn(TestActor.DUMMY_ACTOR);

        client.startActorListener(actor);

        verify(jmsClient).addQueueListener(eq(TestActor.DUMMY_ACTOR), any(MessageListener.class));
        verify(jmsClient).startListening();
    }

    @Test(expected = RuntimeException.class)
    public void start_agent_listener_and_listening_throws() throws JMSException {

        doThrow(new JMSException(new IllegalArgumentException())).when(jmsClient).startListening();

        client.startAgentListener(TEST_AGENT, Constants.AGENTS_ACTOR_NAME, mock(AgentClientListener.class));

        verify(jmsClient).addTopicListener(eq(Constants.AGENTS_ACTOR_NAME), any(MessageListener.class));
        verify(jmsClient).startListening();
    }

    @Test(expected = RuntimeException.class)
    public void start_actor_listener_and_listening_throws() throws JMSException {
        Actor actor = mock(Actor.class);
        when(actor.name()).thenReturn(TestActor.DUMMY_ACTOR);
        doThrow(new JMSException(new IllegalArgumentException())).when(jmsClient).startListening();

        client.startActorListener(actor);

        verify(jmsClient).addQueueListener(eq(TestActor.DUMMY_ACTOR), any(MessageListener.class));
        verify(jmsClient).startListening();
    }

    @Test
    public void send_to_actors_sends_to_jms_queue() throws JMSException {
        Message<String> testMsg = getTestMsg();
        client.sendToActor(TestActor.DUMMY_ACTOR, testMsg);

        verify(jmsClient).sendToQueue(TestActor.DUMMY_ACTOR, testMsg);
    }

    @Test(expected = RuntimeException.class)
    public void send_to_actors_sends_to_jms_queue_throws() throws JMSException {
        Message<String> testMsg = getTestMsg();
        doThrow(new JMSException(new IllegalArgumentException())).when(jmsClient).sendToQueue(anyString(),
                                                                                              any(Serializable.class));

        client.sendToActor(TestActor.DUMMY_ACTOR, testMsg);

        verify(jmsClient).sendToQueue(TestActor.DUMMY_ACTOR, testMsg);
        verifyNoMoreInteractions(jmsClient);
    }

    @Test
    public void close_closes_jms_client() throws JMSException {
        client.close();

        verify(jmsClient).close();
        verifyNoMoreInteractions(jmsClient);
    }

    private Message<String> getTestMsg() {
        return new Message<>(TestActor.DUMMY_ACTOR, "data");
    }
}