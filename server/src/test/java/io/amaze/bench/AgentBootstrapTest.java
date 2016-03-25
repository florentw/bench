package io.amaze.bench;

import io.amaze.bench.client.runtime.agent.AgentBootstrap;
import io.amaze.bench.client.runtime.agent.Constants;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.test.IntegrationTest;
import io.amaze.bench.shared.test.JMSServerRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * Created on 3/5/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
@Category(IntegrationTest.class)
public class AgentBootstrapTest {

    @Rule
    public JMSServerRule server = new JMSServerRule();

    @Test
    public void bootstrap_with_server() throws Exception {
        server.getServer().createQueue(Constants.MASTER_ACTOR_NAME);
        server.getServer().createTopic(Constants.AGENTS_ACTOR_NAME);

        try (final JMSClient client = server.createClient()) {
            client.addQueueListener(Constants.MASTER_ACTOR_NAME, new MessageListener() {
                @Override
                public void onMessage(final Message message) {
                    System.out.println("received MASTER_ACTOR_NAME msg " + message);
                }
            });

            new Thread() {
                @Override
                public void run() {
                    try {
                        AgentBootstrap.main(new String[]{server.getHost(), "" + server.getPort()});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            client.startListening();
            Thread.sleep(3000);
        }
    }

}