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
package io.amaze.bench.client.runtime.orchestrator;

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.shared.jms.FFMQClient;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

/**
 * Created on 3/3/16.
 */
abstract class JMSOrchestratorClient implements OrchestratorClient {

    private final JMSClient client;

    @VisibleForTesting
    JMSOrchestratorClient(@NotNull final JMSClient client) {
        this.client = checkNotNull(client);
    }

    JMSOrchestratorClient(@NotNull final String host, @NotNull final int port) {
        checkNotNull(host);
        checkArgument(port > 0);

        try {
            client = new FFMQClient(host, port);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public final void sendToActor(@NotNull final String to, @NotNull final Message<? extends Serializable> message) {
        checkNotNull(to);
        checkNotNull(message);

        try {
            client.sendToQueue(to, message);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public final void close() {
        client.close();
    }

    final JMSClient getClient() {
        return client;
    }

}
