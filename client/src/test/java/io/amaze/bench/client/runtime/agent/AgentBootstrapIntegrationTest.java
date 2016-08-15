package io.amaze.bench.client.runtime.agent;

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
                    AgentBootstrap.main(new String[]{server.getHost(), "" + server.getPort()});
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

        server.getServer().createQueue(Constants.MASTER_ACTOR_NAME);
        server.getServer().createTopic(Constants.AGENTS_ACTOR_NAME);

        client.addQueueListener(Constants.MASTER_ACTOR_NAME, listener);
        client.startListening();

        agentBootstrapThread.start();

        boolean msgReceived = awaitUninterruptibly(listener.msgReceived, 2, TimeUnit.SECONDS);
        assertTrue(msgReceived);

        // Verify agent sent a message to master
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