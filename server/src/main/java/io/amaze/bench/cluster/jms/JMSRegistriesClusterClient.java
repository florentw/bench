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
package io.amaze.bench.cluster.jms;

import io.amaze.bench.cluster.RegistriesClusterClient;
import io.amaze.bench.cluster.registry.ActorRegistryListener;
import io.amaze.bench.cluster.registry.AgentRegistryListener;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSServer;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.client.runtime.agent.Constants.REGISTRIES_TOPIC;

/**
 * Created on 9/24/16.
 */
public final class JMSRegistriesClusterClient implements RegistriesClusterClient {

    private final JMSClient client;

    public JMSRegistriesClusterClient(@NotNull final JMSServer server, @NotNull final JMSClient client) {
        this.client = checkNotNull(client);

        try {
            server.createTopic(REGISTRIES_TOPIC);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public void startRegistryListeners(@NotNull final AgentRegistryListener agentsListener,
                                       @NotNull final ActorRegistryListener actorsListener) {
        checkNotNull(agentsListener);
        checkNotNull(actorsListener);

        try {
            JMSRegistriesTopicListener msgListener = new JMSRegistriesTopicListener(agentsListener, actorsListener);
            client.addTopicListener(REGISTRIES_TOPIC, msgListener);
            client.startListening();

        } catch (JMSException e) {
            throw propagate(e);
        }
    }
}
