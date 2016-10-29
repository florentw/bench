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
package io.amaze.bench.leader.cluster.jgroups;

import io.amaze.bench.leader.cluster.ResourceManagerClusterClient;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.agent.AgentInputMessage;
import io.amaze.bench.runtime.cluster.jgroups.JgroupsSender;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/23/16.
 */
public final class JgroupsResourceManagerClusterClient implements ResourceManagerClusterClient {

    private final JgroupsSender jgroupsSender;

    public JgroupsResourceManagerClusterClient(@NotNull final JgroupsSender jgroupsSender) {
        this.jgroupsSender = checkNotNull(jgroupsSender);
    }

    @Override
    public void initForActor(@NotNull final ActorKey key) {
        checkNotNull(key);
        // Nothing to do here
    }

    @Override
    public void closeForActor(@NotNull final ActorKey key) {
        checkNotNull(key);
        // Nothing to do here
    }

    @Override
    public void sendToAgent(@NotNull final AgentInputMessage message) {
        checkNotNull(message);

        jgroupsSender.broadcast(message);
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
