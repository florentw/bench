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
package io.amaze.bench.cluster.actor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Configuration object for Actors to be created.
 * Configuration of the {@link io.amaze.bench.api.Reactor} instance is stored in JSON format ({@link #actorJsonConfig})
 */
public final class ActorConfig implements Serializable {

    private final ActorKey key;
    private final String className;
    private final DeployConfig deployConfig;
    private final String actorJsonConfig;

    public ActorConfig(@NotNull final ActorKey key,
                       @NotNull final String className,
                       @NotNull final DeployConfig deployConfig,
                       @NotNull final String actorJsonConfig) {

        this.key = checkNotNull(key);
        this.className = checkNotNull(className);
        this.deployConfig = checkNotNull(deployConfig);
        this.actorJsonConfig = checkNotNull(actorJsonConfig);
    }

    @NotNull
    public ActorKey getKey() {
        return key;
    }

    @NotNull
    public String getClassName() {
        return className;
    }

    @NotNull
    public DeployConfig getDeployConfig() {
        return deployConfig;
    }

    @NotNull
    public String getActorJsonConfig() {
        return actorJsonConfig;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, className, deployConfig, actorJsonConfig);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActorConfig that = (ActorConfig) o;
        return Objects.equals(key, that.key) && //
                Objects.equals(className, that.className) && //
                Objects.equals(deployConfig, that.deployConfig) && //
                Objects.equals(actorJsonConfig, that.actorJsonConfig);
    }

    @Override
    public String toString() {
        return "{\"ActorConfig\":{" + //
                "\"key\":" + key + ", " + //
                "\"className\":\"" + className + "\", " + //
                "\"deployConfig\":" + deployConfig + ", " + //
                "\"actorJsonConfig\":" + actorJsonConfig + "}}";
    }
}
