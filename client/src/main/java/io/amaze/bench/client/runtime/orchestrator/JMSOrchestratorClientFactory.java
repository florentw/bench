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

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/3/16.
 */
public final class JMSOrchestratorClientFactory implements OrchestratorClientFactory {

    private final String jmsServerHost;
    private final int jmsServerPort;

    public JMSOrchestratorClientFactory(@NotNull final String jmsServerHost, final int port) {
        checkNotNull(jmsServerHost);
        checkArgument(port > 0);

        this.jmsServerHost = jmsServerHost;
        this.jmsServerPort = port;
    }

    @Override
    public OrchestratorAgent createForAgent() {
        return new JMSOrchestratorAgent(jmsServerHost, jmsServerPort);
    }

    @Override
    public OrchestratorActor createForActor() {
        return new JMSOrchestratorActor(jmsServerHost, jmsServerPort);
    }

}

