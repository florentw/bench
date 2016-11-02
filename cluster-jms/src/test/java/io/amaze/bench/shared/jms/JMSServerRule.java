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
package io.amaze.bench.shared.jms;

import io.amaze.bench.shared.util.Network;
import org.junit.rules.ExternalResource;

import static com.google.common.base.Throwables.propagate;

/**
 * Junit 4 rule that provides an embedded JMSServer for tests.
 */
public final class JMSServerRule extends ExternalResource {

    static final String DEFAULT_HOST = Network.LOCALHOST;

    private JMSServer server;
    private JMSEndpoint endpoint;

    public JMSEndpoint getEndpoint() {
        return endpoint;
    }

    public JMSServer getServer() {
        return server;
    }

    public JMSClient createClient() {
        try {
            return new FFMQClient(endpoint);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    public void init() {
        int port = Network.findFreePort();
        try {
            endpoint = new JMSEndpoint(DEFAULT_HOST, port);
            server = new FFMQServer(endpoint);
        } catch (JMSException e) {
            propagate(e);
        }
    }

    public void close() {
        if (server != null) {
            try {
                server.close();
            } catch (Exception e) {
                propagate(e);
            }
        }
    }

    @Override
    protected void before() {
        init();
    }

    @Override
    protected void after() {
        close();
    }
}
