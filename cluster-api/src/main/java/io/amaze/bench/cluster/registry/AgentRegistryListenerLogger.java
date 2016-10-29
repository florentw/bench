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

import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.agent.AgentRegistrationMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Decorator to log agent registry events
 */
final class AgentRegistryListenerLogger implements AgentRegistryListener {

    private static final Logger log = LogManager.getLogger();

    private final AgentRegistryListener delegate;

    AgentRegistryListenerLogger(@NotNull final AgentRegistryListener delegate) {
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public void onAgentRegistration(@NotNull final AgentRegistrationMessage msg) {
        checkNotNull(msg);

        log.info("Agent {} registering, msg: {}", msg.getKey(), msg);

        delegate.onAgentRegistration(msg);
    }

    @Override
    public void onAgentSignOff(@NotNull final AgentKey agent) {
        checkNotNull(agent);

        log.info("Agent {} signing off.", agent);

        delegate.onAgentSignOff(agent);
    }

    @Override
    public void onAgentFailed(final AgentKey agent, final Throwable throwable) {
        checkNotNull(agent);
        checkNotNull(throwable);

        log.info("Agent {} failed", agent, throwable);

        delegate.onAgentFailed(agent, throwable);
    }
}
