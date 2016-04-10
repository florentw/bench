package io.amaze.bench.shared.jms;

import com.google.common.util.concurrent.Uninterruptibles;
import io.amaze.bench.shared.test.IntegrationTest;
import io.amaze.bench.shared.test.JMSServerRule;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.amaze.bench.shared.helper.NetworkHelper.LOCALHOST;
import static io.amaze.bench.shared.helper.NetworkHelper.findFreePort;
import static io.amaze.bench.shared.jms.FFMQServerTest.DUMMY_QUEUE;
import static io.amaze.bench.shared.jms.FFMQServerTest.DUMMY_TOPIC;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created on 3/2/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
@Category(IntegrationTest.class)
public final class JMSClientServerTest {

    private static final String DUMMY_PAYLOAD = "Hello";

    @Rule
    public JMSServerRule server = new JMSServerRule();

    @Before
    public void before() throws JMSException {
        server.getServer().createQueue(DUMMY_QUEUE);
    }

    @Test
    public void start_close_client() throws Exception {
        try (JMSClient client = new FFMQClient(server.getHost(), server.getPort())) {
            TestCase.assertNotNull(client);
        }
    }

    @Test(expected = JMSException.class)
    public void start_client_with_wrong_endpoint_throws() throws Exception {
        new FFMQClient(LOCALHOST, findFreePort());
    }

    @Test
    public void close_client_after_server_closed_does_not_throw() throws Exception {
        JMSClient client = new FFMQClient(server.getHost(), server.getPort());
        server.getServer().close();
        client.close();
    }

    @Test
    public void close_client_twice_does_not_throw() throws Exception {
        JMSClient client = new FFMQClient(server.getHost(), server.getPort());
        server.getServer().close();
        client.close();
        client.close();
    }

    @Test
    public void client_starts_listening_with_no_listener_does_not_throw() throws Exception {
        try (JMSClient client = server.createClient()) {
            client.startListening();
        }
    }

    @Test(expected = JMSException.class)
    public void client_starts_listening_after_closing_server_throws() throws Exception {
        try (JMSClient client = server.createClient()) {
            server.getServer().close();
            client.startListening();
        }
    }

    @Test
    public void client_sends_to_queue_and_receives() throws Exception {
        DummyListener listener = new DummyListener(1);
        try (JMSClient client = server.createClient()) {

            client.addQueueListener(DUMMY_QUEUE, listener);
            client.startListening();

            client.sendToQueue(DUMMY_QUEUE, DUMMY_PAYLOAD);

            List<BytesMessage> bytesMessages = listener.awaitMessages();

            assertNotNull(server);
            assertThat(bytesMessages.size(), is(1));
            assertNotNull(bytesMessages.get(0));
            assertThat((String) JMSHelper.objectFromMsg(bytesMessages.get(0)), is(DUMMY_PAYLOAD));
        }
    }

    @Test
    public void client_sends_to_unknown_queue_does_not_throw() throws Exception {
        try (JMSClient client = server.createClient()) {
            client.sendToQueue("None", DUMMY_PAYLOAD);
        }
    }

    @Test(expected = JMSException.class)
    public void client_sends_to_queue_after_server_closed_throws() throws Exception {
        try (JMSClient client = server.createClient()) {
            server.getServer().close();
            client.sendToQueue("None", DUMMY_PAYLOAD);
        }
    }

    @Test(expected = JMSException.class)
    public void client_listens_to_unknown_queue_throws() throws Exception {
        DummyListener listener = new DummyListener(1);
        try (JMSClient client = server.createClient()) {

            client.addQueueListener(DUMMY_TOPIC, listener);
            client.startListening();

            assertNotNull(server);
        }
    }

    @Test
    public void client_sends_five_messages_to_queue_and_receives() throws Exception {
        DummyListener listener = new DummyListener(5);
        try (JMSClient client = server.createClient()) {

            client.addQueueListener(DUMMY_QUEUE, listener);
            client.startListening();

            for (int i = 0; i < 5; i++) {
                client.sendToQueue(DUMMY_QUEUE, i + "");
            }

            List<BytesMessage> bytesMessages = listener.awaitMessages();

            assertNotNull(server);
            assertThat(bytesMessages.size(), is(5));
            assertNotNull(bytesMessages.get(0));

            // Ensure messages are received in the same order they were sent
            for (int i = 0; i < 5; i++) {
                assertThat((String) JMSHelper.objectFromMsg(bytesMessages.get(i)), is(i + ""));
            }
        }
    }

    @Test
    public void client_sends_to_topic_and_receives() throws Exception {
        DummyListener listener = new DummyListener(1);
        try (JMSClient client = server.createClient()) {

            server.getServer().createTopic(DUMMY_TOPIC);

            client.addTopicListener(DUMMY_TOPIC, listener);
            client.startListening();

            client.sendToTopic(DUMMY_TOPIC, DUMMY_PAYLOAD);

            List<BytesMessage> bytesMessages = listener.awaitMessages();

            assertNotNull(server);
            assertThat(bytesMessages.size(), is(1));
            assertNotNull(bytesMessages.get(0));
            assertThat((String) JMSHelper.objectFromMsg(bytesMessages.get(0)), is(DUMMY_PAYLOAD));
        }
    }

    @Test
    public void client_sends_to_unknown_topic_does_not_throw() throws Exception {
        try (JMSClient client = server.createClient()) {

            assertNotNull(server);
            client.sendToTopic(DUMMY_TOPIC, DUMMY_PAYLOAD);
        }
    }

    @Test(expected = JMSException.class)
    public void client_sends_to_topic_after_server_closed_throws() throws Exception {
        try (JMSClient client = server.createClient()) {
            server.getServer().close();
            client.sendToTopic("None", DUMMY_PAYLOAD);
        }
    }

    @Test(expected = JMSException.class)
    public void client_listens_to_unknown_topic_throws() throws Exception {
        DummyListener listener = new DummyListener(1);
        try (JMSClient client = server.createClient()) {

            client.addTopicListener(DUMMY_TOPIC, listener);
            client.startListening();

            assertNotNull(server);
        }
    }

    @Test
    public void client_sends_five_messages_to_topic_and_receives() throws Exception {
        DummyListener listener = new DummyListener(5);
        try (JMSClient client = server.createClient()) {

            server.getServer().createTopic(DUMMY_TOPIC);
            client.addTopicListener(DUMMY_TOPIC, listener);
            client.startListening();

            for (int i = 0; i < 5; i++) {
                client.sendToTopic(DUMMY_TOPIC, i + "");
            }

            List<BytesMessage> bytesMessages = listener.awaitMessages();

            assertNotNull(server);
            assertThat(bytesMessages.size(), is(5));
            assertNotNull(bytesMessages.get(0));

            // Ensure messages are received in the same order they were sent
            for (int i = 0; i < 5; i++) {
                assertThat((String) JMSHelper.objectFromMsg(bytesMessages.get(i)), is(i + ""));
            }
        }
    }

    private static class DummyListener implements MessageListener {
        private final CountDownLatch latch;
        private final List<BytesMessage> messages = new ArrayList<>();

        DummyListener(int expectedNbOfMessages) {
            latch = new CountDownLatch(expectedNbOfMessages);
        }

        @Override
        public void onMessage(final Message message) {
            messages.add((BytesMessage) message);
            latch.countDown();
        }

        List<BytesMessage> awaitMessages() {
            Uninterruptibles.awaitUninterruptibly(latch, 2, TimeUnit.SECONDS);
            return messages;
        }
    }

}
