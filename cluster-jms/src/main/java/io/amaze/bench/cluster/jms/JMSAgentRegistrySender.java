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
package io.amaze.bench.cluster.jms;

import io.amaze.bench.cluster.Message;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.agent.AgentLifecycleMessage;
import io.amaze.bench.cluster.agent.AgentRegistrySender;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.cluster.agent.Constants.AGENT_REGISTRY_TOPIC;

/**
 * Created on 10/18/16.
 */
public final class JMSAgentRegistrySender implements AgentRegistrySender {

    private final JMSClient client;
    private final AgentKey agent;

    public JMSAgentRegistrySender(@NotNull final JMSClient client, @NotNull final AgentKey agent) {
        this.client = checkNotNull(client);
        this.agent = checkNotNull(agent);
    }

    @Override
    public void send(@NotNull final AgentLifecycleMessage message) {
        checkNotNull(message);

        try {
            client.sendToTopic(AGENT_REGISTRY_TOPIC, new Message<>(agent.getName(), message));
        } catch (JMSException e) {
            throw propagate(e);
        }
    }
}
