package io.amaze.bench.shared.jms;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.shared.helper.NetworkHelper;
import io.amaze.bench.shared.test.JMSServerRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created on 3/2/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class FFMQServerTest {

    static final String DUMMY_QUEUE = "DummyQueue";
    static final String DUMMY_TOPIC = "DummyTopic";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public JMSServerRule server = new JMSServerRule();

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(FFMQServer.class);
        tester.testAllPublicInstanceMethods(server);
    }

    @Test
    public void start_stop_server() throws Exception {
        assertNotNull(server);
    }

    @Test
    public void start_server_on_bound_port_throws() throws Exception {
        expectedException.expect(JMSException.class);
        expectedException.expectMessage("Cannot create server socket");

        new FFMQServer(JMSServerRule.DEFAULT_HOST, server.getPort());
    }

    @Test
    public void start_server_deletes_properties_of_previous_zombie() throws Exception {
        String queuePropFileName = FFMQServer.DATA_DIR_PATH + File.separator + FFMQServer.QUEUE_PREFIX + "test" + FFMQServer.PROP_SUFFIX;
        String topicPropFileName = FFMQServer.DATA_DIR_PATH + File.separator + FFMQServer.TOPIC_PREFIX + "test" + FFMQServer.PROP_SUFFIX;
        File queuePropFile = new File(queuePropFileName);
        File topicPropFile = new File(topicPropFileName);
        queuePropFile.createNewFile(); // NOSONAR
        topicPropFile.createNewFile(); // NOSONAR

        new FFMQServer(JMSServerRule.DEFAULT_HOST, NetworkHelper.findFreePort());

        assertThat(queuePropFile.exists(), is(false));
        assertThat(topicPropFile.exists(), is(false));
    }

    @Test
    public void create_queue() throws Exception {
        server.getServer().createQueue(DUMMY_QUEUE);
    }

    @Test(expected = JMSException.class)
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
    public void delete_unknown_queue_does_not_throw() {
        server.getServer().deleteQueue(DUMMY_QUEUE);
    }

    @Test
    public void delete_invalid_queue_does_not_throw() {
        server.getServer().deleteQueue("$%?.");
    }

    @Test
    public void create_topic() throws Exception {
        server.getServer().createTopic(DUMMY_TOPIC);
    }

    @Test(expected = JMSException.class)
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
    public void delete_unknown_topic_does_not_throw() {
        server.getServer().deleteTopic(DUMMY_TOPIC);
    }

    @Test
    public void delete_invalid_topic_does_not_throw() {
        server.getServer().deleteTopic("$%?");
    }

}