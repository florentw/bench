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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 4/6/16.
 */
public final class DeployConfig implements Serializable {

    private final boolean forked;
    private final List<String> preferredHosts;

    public DeployConfig(final boolean forked, @NotNull final List<String> preferredHosts) {
        this.forked = forked;
        this.preferredHosts = checkNotNull(preferredHosts);
    }

    @NotNull
    public List<String> getPreferredHosts() {
        return Collections.unmodifiableList(preferredHosts);
    }

    public boolean isForked() {
        return forked;
    }

    @Override
    public int hashCode() {
        return Objects.hash(forked, preferredHosts);
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
                Objects.equals(preferredHosts, that.preferredHosts);
    }

    @Override
    public String toString() {
        return "{\"DeployConfig\":{" + //
                "\"forked\":\"" + forked + "\"" + ", " + //
                "\"preferredHosts\":\"" + preferredHosts + "\"}}";
    }

}
