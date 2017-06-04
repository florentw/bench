/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
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


import io.amaze.bench.cluster.Endpoint;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * This bean provides information that is discovered at runtime after deployment on the actor's side.
 */
public final class ActorDeployInfo implements Serializable {

    private final int pid;
    private final Endpoint endpoint;
    private final List<String> command;

    public ActorDeployInfo(@NotNull final Endpoint endpoint, final int pid) {
        this(endpoint, pid, Collections.emptyList());
    }

    public ActorDeployInfo(@NotNull final Endpoint endpoint, final int pid, @NotNull final List<String> command) {
        this.endpoint = requireNonNull(endpoint);
        checkArgument(pid > 0, "Invalid pid " + pid);
        this.pid = pid;
        this.command = requireNonNull(command);
    }

    @NotNull
    public <T extends Endpoint> T getEndpoint() {
        return (T) endpoint;
    }

    public int getPid() {
        return pid;
    }

    @NotNull
    public List<String> getCommand() {
        return command;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid, endpoint, command);
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
        return pid == that.pid && //
                Objects.equals(endpoint, that.endpoint) && //
                Objects.equals(command, that.command);
    }

    @Override
    public String toString() {
        return "{\"ActorDeployInfo\":{" + //
                "\"pid\":\"" + pid + "\"" + ", " + //
                "\"endpoint\":" + endpoint + ", " + //
                "\"command\":" + command + "}}";
    }
}
