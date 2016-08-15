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
package io.amaze.bench.shared.test;

import com.google.common.base.Throwables;
import io.amaze.bench.shared.helper.NetworkHelper;
import io.amaze.bench.shared.jms.*;
import org.junit.rules.ExternalResource;

/**
 * Junit 4 rule that provides an embedded JMSServer for tests.
 * <p>
 * Created on 3/19/16.
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
