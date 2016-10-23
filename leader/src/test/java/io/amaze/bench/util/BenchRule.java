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
package io.amaze.bench.util;

import com.typesafe.config.Config;
import io.amaze.bench.leader.cluster.Actors;
import io.amaze.bench.leader.cluster.LeaderClusterClientFactory;
import io.amaze.bench.leader.cluster.ResourceManager;
import io.amaze.bench.leader.cluster.ResourceManagerClusterClient;
import io.amaze.bench.leader.cluster.jms.JMSLeaderClusterClientFactory;
import io.amaze.bench.leader.cluster.registry.MetricsRepository;
import io.amaze.bench.leader.cluster.registry.MetricsRepositoryClusterClient;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.actor.ActorManagers;
import io.amaze.bench.runtime.agent.Agents;
import io.amaze.bench.runtime.cluster.ActorClusterClient;
import io.amaze.bench.runtime.cluster.ActorSender;
import io.amaze.bench.runtime.cluster.ClusterClientFactory;
import io.amaze.bench.runtime.cluster.ClusterClients;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.runtime.cluster.registry.AgentRegistry;
import io.amaze.bench.runtime.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.util.Network;
import org.junit.rules.ExternalResource;

/**
 * JUnit 4 rule that allows to instantiate a complete ecosystem to run integration tests.
 */
public final class BenchRule extends ExternalResource {

    private AgentRegistry agentRegistry;
    private ActorRegistry actorRegistry;
    private ResourceManager resourceManager;
    private ActorSender actorSender;

    private Agents agents;
    private Actors actors;

    private MetricsRepository metricsRepository;
    private ActorRegistryClusterClient actorRegistryClient;
    private AgentRegistryClusterClient agentRegistryClient;
    private ResourceManagerClusterClient resourceManagerClient;
    private MetricsRepositoryClusterClient metricsClusterClient;
    private LeaderClusterClientFactory leaderClientFactory;

    public AgentRegistry agentRegistry() {
        return agentRegistry;
    }

    public ActorRegistry actorRegistry() {
        return actorRegistry;
    }

    public ResourceManager resourceManager() {
        return resourceManager;
    }

    public Agents agents() {
        return agents;
    }

    public Actors actors() {
        return actors;
    }

    public ActorSender actorSender() {
        return actorSender;
    }

    public MetricsRepository metricsRepository() {
        return metricsRepository;
    }

    @Override
    protected void before() {
        JMSEndpoint endpoint = new JMSEndpoint(Network.LOCALHOST, Network.findFreePort());
        Config leaderConfig = ClusterConfigs.leaderJmsClusterConfig(endpoint, JMSLeaderClusterClientFactory.class);
        actorRegistry = new ActorRegistry();
        leaderClientFactory = ClusterClients.newFactory(LeaderClusterClientFactory.class, leaderConfig, actorRegistry);

        Config clusterConfig = ClusterConfigs.jmsClusterConfig(endpoint);
        ClusterClientFactory ruleFactory = ClusterClients.newFactory(ClusterClientFactory.class,
                                                                     clusterConfig, new ActorRegistry());

        actorRegistryClient = leaderClientFactory.createForActorRegistry();

        agentRegistry = new AgentRegistry();
        agentRegistryClient = leaderClientFactory.createForAgentRegistry(agentRegistry);

        resourceManagerClient = leaderClientFactory.createForResourceManager();
        resourceManager = new ResourceManager(resourceManagerClient, agentRegistry);

        ActorClusterClient clusterClient = ruleFactory.createForActor(new ActorKey("system-test"));
        actorSender = clusterClient.actorSender();

        ClusterClientFactory clusterClientFactory = ClusterClients.newFactory(ClusterClientFactory.class,
                                                                              clusterConfig,
                                                                              new ActorRegistry());

        agents = new Agents(new ActorManagers(), clusterClientFactory, agentRegistry);
        actors = new Actors(actorSender, resourceManager, actorRegistry);

        metricsRepository = new MetricsRepository();
        metricsClusterClient = leaderClientFactory.createForMetricsRepository(metricsRepository);
    }

    @Override
    protected void after() {
        resourceManagerClient.close();
        metricsClusterClient.close();
        actorRegistryClient.close();
        agentRegistryClient.close();

        leaderClientFactory.close();
    }

}
