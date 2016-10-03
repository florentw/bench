/*
 * Copyright 2016 Florent Weber <florent.weber@gmail.com>
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
package io.amaze.bench.runtime.agent;

import com.google.common.base.Throwables;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.test.IntegrationTest;
import io.amaze.bench.shared.test.JMSServerRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created on 3/5/16.
 */
@Category(IntegrationTest.class)
public final class AgentBootstrapIntegrationTest {

    @Rule
    public final JMSServerRule server = new JMSServerRule();

    private Thread agentBootstrapThread;
    private JMSClient client;

    @Before
    public void before() throws JMSException {
        agentBootstrapThread = new Thread() {
            @Override
            public void run() {
                try {
                    AgentBootstrap.main(new String[]{server.getEndpoint().getHost(), //
                            Integer.toString(server.getEndpoint().getPort())});
                } catch (Exception e) {
                    Throwables.propagate(e);
                }
            }
        };

        client = server.createClient();
    }

    @After
    public void after() {
        client.close();
    }

    @Test
    public void bootstrap_with_server() throws Exception {
        SyncListener listener = spy(new SyncListener());

        server.getServer().createTopic(Constants.AGENT_REGISTRY_TOPIC);
        server.getServer().createTopic(Constants.AGENTS_TOPIC);

        client.addTopicListener(Constants.AGENT_REGISTRY_TOPIC, listener);
        client.startListening();

        agentBootstrapThread.start();

        boolean msgReceived = awaitUninterruptibly(listener.msgReceived, 2, TimeUnit.SECONDS);
        assertTrue(msgReceived);

        // Verify agent sent a message to registry
        verify(listener).onMessage(any(Message.class));
        verifyNoMoreInteractions(listener);
    }

    private static class SyncListener implements MessageListener {
        private final CountDownLatch msgReceived = new CountDownLatch(1);

        @Override
        public void onMessage(final Message message) {
            msgReceived.countDown();
        }
    }

}