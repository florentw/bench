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

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Deployment configuration for actors.
 */
public final class DeployConfig implements Serializable {

    private final boolean forked;
    private final List<String> preferredHosts;
    private final List<String> jvmArguments;

    public DeployConfig(final boolean forked, @NotNull final List<String> preferredHosts) {
        this(forked, preferredHosts, Collections.emptyList());
    }

    public DeployConfig(final boolean forked,
                        @NotNull final List<String> preferredHosts,
                        @NotNull final List<String> jvmArguments) {
        this.forked = forked;
        this.preferredHosts = requireNonNull(preferredHosts);
        this.jvmArguments = requireNonNull(jvmArguments);
    }

    /**
     * If preferred hosts are specified, the resource manager will try to spawn the actor instances on them.
     *
     * @return The list of preferred hosts for this actor instance.
     */
    @NotNull
    public List<String> getPreferredHosts() {
        return Collections.unmodifiableList(preferredHosts);
    }

    /**
     * Indicates whether the actor instance must be created in its own JVM, or should be embedded in the agent's JVM.
     *
     * @return true if the actor instance must be in its own JVM.
     */
    public boolean isForked() {
        return forked;
    }

    /**
     * Allows to specify JVM arguments to be passed to the JVM of a forked actor.
     * If {@link #isForked()} is false, these parameters are ignored.
     *
     * @return A non-null list of arguments to be passed to the JVM of the forked actor.
     */
    @NotNull
    public List<String> getJvmArguments() {
        return jvmArguments;
    }

    @Override
    public int hashCode() {
        return Objects.hash(forked, preferredHosts, jvmArguments);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeployConfig that = (DeployConfig) o;
        return forked == that.forked && //
                Objects.equals(preferredHosts, that.preferredHosts) && //
                Objects.equals(jvmArguments, that.jvmArguments);
    }

    @Override
    public String toString() {
        return "{\"DeployConfig\":{" + //
                "\"forked\":\"" + forked + "\"" + ", " + //
                "\"preferredHosts\":" + preferredHosts + ", " + //
                "\"jvmArguments\":" + jvmArguments + "}}";
    }
}
