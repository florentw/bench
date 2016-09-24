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

import io.amaze.bench.shared.metric.SystemConfig;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/29/16.
 */
public final class RegisteredAgent {

    private final String name;
    private final SystemConfig systemConfig;
    private final long creationTime;

    RegisteredAgent(@NotNull final String name, @NotNull final SystemConfig systemConfig,
                    @NotNull final long creationTime) {

        this.name = checkNotNull(name);
        this.systemConfig = checkNotNull(systemConfig);
        this.creationTime = checkNotNull(creationTime);
    }

    public String getName() {
        return name;
    }

    public SystemConfig getSystemConfig() {
        return systemConfig;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
