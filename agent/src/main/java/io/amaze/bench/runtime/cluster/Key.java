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
package io.amaze.bench.runtime.cluster;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Classes implementing {@link Key} are used to identify uniquely an entity in the cluster:
 * <ul>
 * <li>Identifying Agents uniquely throughout the cluster.</li>
 * <li>Identifying Actors uniquely throughout the cluster.</li>
 * </ul>
 * Classes implementing it must:
 * <ul>
 * <li>be immutable.</li>
 * <li>be Serializable.</li>
 * <li>implement equals() and hashCode() properly.</li>
 * </ul>
 */
@FunctionalInterface
public interface Key extends Serializable {

    /**
     * @return A label identifying uniquely this entity throughout the cluster.
     */
    @NotNull
    String getName();

}
