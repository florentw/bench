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
package io.amaze.bench.runtime.cluster.jgroups;

import io.amaze.bench.runtime.cluster.AgentRegistrySender;
import io.amaze.bench.runtime.cluster.agent.AgentLifecycleMessage;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/18/16.
 */
public final class JgroupsAgentRegistrySender implements AgentRegistrySender {

    private final JgroupsSender jgroupsSender;

    public JgroupsAgentRegistrySender(@NotNull final JgroupsSender jgroupsSender) {
        this.jgroupsSender = checkNotNull(jgroupsSender);
    }

    @Override
    public void send(@NotNull final AgentLifecycleMessage message) {
        checkNotNull(message);

        jgroupsSender.broadcast(message);
    }
}
