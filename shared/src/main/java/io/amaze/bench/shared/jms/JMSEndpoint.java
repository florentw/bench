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

import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 9/4/16.
 */
public final class JMSEndpoint implements Serializable {

    private final String host;
    private final int port;

    public JMSEndpoint(final String host, final int port) {
        checkArgument(port > 0, "Port must be a non-zero positive integer.");

        this.host = checkNotNull(host);
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
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
