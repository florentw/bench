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
package io.amaze.bench.runtime.agent;

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

    private final String name;
    private final SystemConfig systemConfig;
    private final long creationTime;

    public AgentRegistrationMessage(@NotNull final String name,
                                    @NotNull final SystemConfig systemConfig,
                                    final long creationTime) {
        this.name = checkNotNull(name);
        this.systemConfig = checkNotNull(systemConfig);
        this.creationTime = creationTime;
    }

    public static AgentRegistrationMessage create(@NotNull final String name) {
        checkNotNull(name);

        SystemConfig systemConfig = SystemConfigs.get();
        long creationTime = System.currentTimeMillis();
        return new AgentRegistrationMessage(name, systemConfig, creationTime);
    }

    public String getName() {
        return name;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public SystemConfig getSystemConfig() {
        return systemConfig;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
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
        return Objects.equals(name, that.name);
    }

    @Override
    public String toString() {
        return "{\"AgentRegistrationMessage\":{" + //
                "\"name\":\"" + name + "\"" + ", " + //
                "\"systemConfig\":" + systemConfig + ", " + //
                "\"creationTime\":\"" + creationTime + "\"" + "}}";
    }
}