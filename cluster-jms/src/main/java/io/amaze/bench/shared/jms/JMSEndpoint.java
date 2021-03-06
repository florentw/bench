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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.amaze.bench.cluster.Endpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * Created on 9/4/16.
 */
public final class JMSEndpoint implements Endpoint {

    private static final String SERVER_HOST = "jmsServerHost";
    private static final String SERVER_PORT = "jmsServerPort";

    private final String host;
    private final int port;

    public JMSEndpoint(final Config clusterConfig) {
        this(clusterConfig.getString(SERVER_HOST), //
             clusterConfig.getInt(SERVER_PORT));
    }

    public JMSEndpoint(final String host, final int port) {
        checkArgument(port > 0, "Port must be a non-zero positive integer.");

        this.host = requireNonNull(host);
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Config toConfig() {
        Map<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put(SERVER_HOST, host);
        propertiesMap.put(SERVER_PORT, port);
        return ConfigFactory.parseMap(propertiesMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JMSEndpoint that = (JMSEndpoint) o;
        return port == that.port && Objects.equals(host, that.host);
    }

    @Override
    public String toString() {
        return "{" + "\"host\":\"" + host + "\"" + ", \"port\":\"" + port + "\"" + "}";
    }
}
