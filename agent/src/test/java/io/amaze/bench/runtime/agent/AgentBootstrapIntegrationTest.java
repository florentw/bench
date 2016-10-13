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
import io.amaze.bench.shared.util.Files;
import io.amaze.bench.util.AgentClusterRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.io.File;
import java.io.IOException;
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
    public final AgentClusterRule cluster = new AgentClusterRule();
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Thread agentBootstrapThread;
    private JMSClient client;

    @Before
    public void before() throws JMSException, IOException {
        AgentConfig agentConfig = cluster.agentConfig();
        File configFile = temporaryFolder.newFile();
        Files.writeTo(configFile, agentConfig.toJson());
        agentBootstrapThread = new Thread() {
            @Override
            public void run() {
                try {
                    AgentBootstrap.main(new String[]{configFile.getAbsolutePath()});
                } catch (Exception e) {
                    Throwables.propagate(e);
                }
            }
        };

        client = cluster.jmsServerRule().createClient();
    }

    @After
    public void after() {
        client.close();
    }

    @Test
    public void bootstrap_with_server() throws Exception {
        SyncListener listener = spy(new SyncListener());

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