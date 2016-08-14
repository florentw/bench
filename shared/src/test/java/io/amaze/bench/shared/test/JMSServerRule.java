package io.amaze.bench.shared.test;

import com.google.common.base.Throwables;
import io.amaze.bench.shared.helper.NetworkHelper;
import io.amaze.bench.shared.jms.*;
import org.junit.rules.ExternalResource;

/**
 * Junit 4 rule that provides an embedded JMSServer for tests.
 * <p>
 * Created on 3/19/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class JMSServerRule extends ExternalResource {

    public static final String DEFAULT_HOST = NetworkHelper.LOCALHOST;

    private JMSServer server;
    private int port;

    public String getHost() {
        return DEFAULT_HOST;
    }

    public int getPort() {
        return port;
    }

    public JMSServer getServer() {
        return server;
    }

    public JMSClient createClient() {
        try {
            return new FFMQClient(DEFAULT_HOST, port);
        } catch (JMSException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    protected void before() {
        port = NetworkHelper.findFreePort();
        try {
            server = new FFMQServer(DEFAULT_HOST, port);
        } catch (JMSException e) {
            Throwables.propagate(e);
        }
    }

    @Override
    protected void after() {
        if (server != null) {
            try {
                server.close();
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        }
    }
}
