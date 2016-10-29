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
package io.amaze.bench.runtime.cluster.jms;

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.runtime.agent.AgentClientListener;
import io.amaze.bench.runtime.agent.AgentKey;
import io.amaze.bench.runtime.cluster.ActorRegistrySender;
import io.amaze.bench.runtime.cluster.ActorSender;
import io.amaze.bench.runtime.cluster.AgentClusterClient;
import io.amaze.bench.runtime.cluster.AgentRegistrySender;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSException;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.runtime.agent.Constants.AGENTS_TOPIC;

/**
 * Created on 4/24/16.
 */
final class JMSAgentClusterClient extends JMSClusterClient implements AgentClusterClient {

    private final AgentKey agent;

    @VisibleForTesting
    JMSAgentClusterClient(@NotNull final JMSClient client, @NotNull final AgentKey agent) {
        super(client);
        this.agent = checkNotNull(agent);
    }

    JMSAgentClusterClient(@NotNull final JMSEndpoint endpoint, @NotNull final AgentKey agent) {
        super(endpoint);
        this.agent = checkNotNull(agent);
    }

    @Override
    public void startAgentListener(@NotNull final AgentKey agentKey, @NotNull final AgentClientListener listener) {
        checkNotNull(agentKey);
        checkNotNull(listener);

        try {
            getClient().addTopicListener(AGENTS_TOPIC, new JMSAgentMessageListener(agentKey, listener));
            getClient().startListening();
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public AgentRegistrySender agentRegistrySender() {
        return new JMSAgentRegistrySender(getClient(), agent);
    }

    @Override
    public ActorRegistrySender actorRegistrySender() {
        return new JMSActorRegistrySender(getClient(), agent.getName());
    }

    @Override
    public ActorSender actorSender() {
        return new JMSActorSender(getClient());
    }

}
