package io.amaze.bench.shared.test;

import com.google.common.base.Throwables;
import io.amaze.bench.shared.helper.NetworkHelper;
import io.amaze.bench.shared.jms.FFMQClient;
import io.amaze.bench.shared.jms.FFMQServer;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSServer;
import org.junit.rules.ExternalResource;

import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Junit 4 rule that provides an embedded JMSServer for tests.
 * <p>
 * Created on 3/19/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class JMSServerRule extends ExternalResource {

    public static final String DEFAULT_HOST = "localhost";

    private JMSServer server;
    private int port;

    public String getHost() {
        return DEFAULT_HOST;
    }

    public int getPort() {
        return port;
    }

    @Override
    protected void before() throws Throwable {
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

    public JMSServer getServer() {
        return server;
    }

    public JMSClient createClient() {
        try {
            return new FFMQClient(DEFAULT_HOST, port);
        } catch (NamingException | JMSException e) {
            throw Throwables.propagate(e);
        }
    }
}
