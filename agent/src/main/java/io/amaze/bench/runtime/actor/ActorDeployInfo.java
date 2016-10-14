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
package io.amaze.bench.runtime.actor;


import io.amaze.bench.Endpoint;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 9/17/16.
 */
public final class ActorDeployInfo implements Serializable {

    private final int pid;
    private final Endpoint endpoint;

    public ActorDeployInfo(@NotNull final Endpoint endpoint, final int pid) {
        this.endpoint = checkNotNull(endpoint);
        checkArgument(pid > 0, "Invalid pid " + pid);
        this.pid = pid;
    }

    @NotNull
    public <T extends Endpoint> T getEndpoint() {
        return (T) endpoint;
    }

    public int getPid() {
        return pid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActorDeployInfo that = (ActorDeployInfo) o;
        return pid == that.pid;
    }

    @Override
    public String toString() {
        return "{\"ActorDeployInfo\":{" +  //
                "\"pid\":\"" + pid + "\"" + ", " + //
                "\"endpoint\":" + endpoint + "}}";
    }
}
