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
package io.amaze.bench.shared.jms;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.shared.util.Network;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created on 3/2/16.
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
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Cannot create server socket");

        new FFMQServer(server.getEndpoint());
    }

    @Test
    public void start_server_deletes_properties_of_previous_zombie() throws Exception {
        String queuePropFileName = FFMQServer.DATA_DIR_PATH + File.separator + FFMQServer.QUEUE_PREFIX + "test" + FFMQServer.PROP_SUFFIX;
        String topicPropFileName = FFMQServer.DATA_DIR_PATH + File.separator + FFMQServer.TOPIC_PREFIX + "test" + FFMQServer.PROP_SUFFIX;
        File queuePropFile = new File(queuePropFileName);
        File topicPropFile = new File(topicPropFileName);
        queuePropFile.createNewFile(); // NOSONAR
        topicPropFile.createNewFile(); // NOSONAR

        JMSEndpoint endpoint = new JMSEndpoint(JMSServerRule.DEFAULT_HOST, Network.findFreePort());
        new FFMQServer(endpoint);

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