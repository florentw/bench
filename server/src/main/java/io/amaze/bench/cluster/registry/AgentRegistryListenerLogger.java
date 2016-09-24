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
package io.amaze.bench.cluster.registry;

import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/30/16.
 */
final class AgentRegistryListenerLogger implements AgentRegistryListener {

    private static final Logger LOG = LoggerFactory.getLogger(AgentRegistryListenerLogger.class);

    private final AgentRegistryListener delegate;

    AgentRegistryListenerLogger(@NotNull final AgentRegistryListener delegate) {
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public void onAgentRegistration(@NotNull final AgentRegistrationMessage msg) {
        checkNotNull(msg);

        LOG.info(String.format("Agent \"%s\" registering, msg: %s", msg.getName(), msg));

        delegate.onAgentRegistration(msg);
    }

    @Override
    public void onAgentSignOff(@NotNull final String agent) {
        checkNotNull(agent);

        LOG.info(String.format("Agent \"%s\" signing off.", agent));

        delegate.onAgentSignOff(agent);
    }
}
