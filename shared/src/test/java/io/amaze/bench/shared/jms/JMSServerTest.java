package io.amaze.bench.shared.jms;

import io.amaze.bench.shared.test.JMSServerRule;
import org.junit.Rule;
import org.junit.Test;

import javax.naming.NameAlreadyBoundException;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created on 3/2/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class JMSServerTest {

    static final String DUMMY_QUEUE = "DummyQueue";
    static final String DUMMY_TOPIC = "DummyTopic";
    @Rule
    public JMSServerRule server = new JMSServerRule();

    @Test
    public void start_stop_server() throws Exception {
        assertNotNull(server);
    }

    @Test(expected = Exception.class)
    public void start_server_on_bound_port_throws() throws Exception {
        new FFMQServer(JMSServerRule.DEFAULT_HOST, server.getPort());
    }

    @Test
    public void create_queue() throws Exception {
        server.getServer().createQueue(DUMMY_QUEUE);
    }

    @Test(expected = NameAlreadyBoundException.class)
    public void create_queue_twice_throws() throws Exception {
        server.getServer().createQueue(DUMMY_QUEUE);
        server.getServer().createQueue(DUMMY_QUEUE);
    }

    @Test
    public void create_delete_queue() throws Exception {
        server.getServer().createQueue(DUMMY_QUEUE);
        server.getServer().deleteQueue(DUMMY_QUEUE);
    }

    @Test
    public void delete_unknown_queue() throws Exception {
        server.getServer().deleteQueue(DUMMY_QUEUE);
    }

    @Test
    public void create_topic() throws Exception {
        server.getServer().createTopic(DUMMY_TOPIC);
    }

    @Test(expected = NameAlreadyBoundException.class)
    public void create_topic_twice_throws() throws Exception {
        server.getServer().createTopic(DUMMY_TOPIC);
        server.getServer().createTopic(DUMMY_TOPIC);
    }

    @Test
    public void create_delete_topic() throws Exception {
        server.getServer().createTopic(DUMMY_TOPIC);
        server.getServer().deleteTopic(DUMMY_TOPIC);
    }

    @Test
    public void delete_unknown_topic() throws Exception {
        server.getServer().deleteTopic(DUMMY_TOPIC);
    }
}