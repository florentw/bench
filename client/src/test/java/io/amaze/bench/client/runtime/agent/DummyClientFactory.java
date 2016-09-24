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
package io.amaze.bench.client.runtime.agent;

import io.amaze.bench.client.runtime.cluster.ActorClusterClient;
import io.amaze.bench.client.runtime.cluster.AgentClusterClient;
import io.amaze.bench.client.runtime.cluster.ClusterClientFactory;

/**
 * Created on 3/30/16.
 */
public final class DummyClientFactory implements ClusterClientFactory {

    private final AgentClusterClient agentClient;
    private final ActorClusterClient actorClient;

    public DummyClientFactory(final AgentClusterClient agentClient, final ActorClusterClient actorClient) {
        this.agentClient = agentClient;
        this.actorClient = actorClient;
    }

    @Override
    public AgentClusterClient createForAgent(String agent) {
        return agentClient;
    }

    @Override
    public ActorClusterClient createForActor(String actor) {
        return actorClient;
    }
}
