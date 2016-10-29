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
package io.amaze.bench.runtime.cluster.registry;

import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.cluster.agent.AgentKey;
import io.amaze.bench.shared.metric.SystemConfig;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/29/16.
 */
public final class RegisteredAgent implements Serializable {

    private final AgentKey agentKey;
    private final SystemConfig systemConfig;
    private final Endpoint endpoint;
    private final long creationTime;

    public RegisteredAgent(@NotNull final AgentKey agentKey,
                           @NotNull final SystemConfig systemConfig,
                           @NotNull final long creationTime,
                           @NotNull final Endpoint endpoint) {

        this.agentKey = checkNotNull(agentKey);
        this.systemConfig = checkNotNull(systemConfig);
        this.creationTime = checkNotNull(creationTime);
        this.endpoint = checkNotNull(endpoint);
    }

    public AgentKey getAgentKey() {
        return agentKey;
    }

    public SystemConfig getSystemConfig() {
        return systemConfig;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentKey, systemConfig, endpoint, creationTime);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegisteredAgent that = (RegisteredAgent) o;
        return creationTime == that.creationTime && //
                Objects.equals(agentKey, that.agentKey) && //
                Objects.equals(systemConfig, that.systemConfig) && //
                Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public String toString() {
        return "{\"RegisteredAgent\":{" + //
                "\"agentKey\":\"" + agentKey + "\"" + ", " + //
                "\"systemConfig\":" + systemConfig + ", " + //
                "\"endpoint\":" + endpoint + ", " + //
                "\"creationTime\":\"" + creationTime + "\"" + "}}";
    }
}
