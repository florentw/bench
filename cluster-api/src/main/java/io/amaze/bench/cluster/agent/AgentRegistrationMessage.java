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
package io.amaze.bench.cluster.agent;

import io.amaze.bench.Endpoint;
import io.amaze.bench.shared.metric.SystemConfig;
import io.amaze.bench.shared.metric.SystemConfigs;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Message sent by an agent to the agent registry after startup for registration.
 */
public final class AgentRegistrationMessage implements Serializable {

    private final AgentKey key;
    private final SystemConfig systemConfig;
    private final Endpoint endpoint;
    private final long creationTime;

    public AgentRegistrationMessage(@NotNull final AgentKey key,
                                    @NotNull final SystemConfig systemConfig,
                                    @NotNull final Endpoint endpoint,
                                    final long creationTime) {
        this.key = checkNotNull(key);
        this.systemConfig = checkNotNull(systemConfig);
        this.endpoint = checkNotNull(endpoint);
        this.creationTime = creationTime;
    }

    public static AgentRegistrationMessage create(@NotNull final AgentKey key, @NotNull final Endpoint endpoint) {
        checkNotNull(key);

        SystemConfig systemConfig = SystemConfigs.get();
        long creationTime = System.currentTimeMillis();
        return new AgentRegistrationMessage(key, systemConfig, endpoint, creationTime);
    }

    public AgentKey getKey() {
        return key;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public SystemConfig getSystemConfig() {
        return systemConfig;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgentRegistrationMessage that = (AgentRegistrationMessage) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public String toString() {
        return "{\"AgentRegistrationMessage\":{" + //
                "\"key\":" + key + ", " + //
                "\"systemConfig\":" + systemConfig + ", " + //
                "\"endpoint\":" + endpoint + ", " + //
                "\"creationTime\":\"" + creationTime + "\"" + "}}";
    }
}
