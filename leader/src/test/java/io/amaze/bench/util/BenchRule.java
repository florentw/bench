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
import io.amaze.bench.cluster.ClusterClientFactory;
import io.amaze.bench.cluster.ClusterClients;
import io.amaze.bench.cluster.actor.ActorSender;
import io.amaze.bench.cluster.leader.LeaderClusterClientFactory;
import io.amaze.bench.cluster.leader.ResourceManagerClusterClient;
import io.amaze.bench.cluster.leader.jgroups.JgroupsLeaderClusterClientFactory;
import io.amaze.bench.cluster.leader.jms.JMSLeaderClusterClientFactory;
import io.amaze.bench.cluster.leader.registry.*;
import io.amaze.bench.leader.Actors;
import io.amaze.bench.leader.ResourceManager;
import io.amaze.bench.runtime.actor.ActorManagers;
import io.amaze.bench.runtime.agent.Agents;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.util.Network;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.ExternalResource;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * JUnit 4 rule that allows to instantiate a complete ecosystem to run integration tests.
 */
public final class BenchRule extends ExternalResource {
    private static final Logger log = LogManager.getLogger();

    private final Config leaderConfig;
    private final Config clusterConfig;

    private AgentRegistry agentRegistry;
    private ActorRegistry actorRegistry;
    private ResourceManager resourceManager;

    private Agents agents;
    private Actors actors;

    private MetricsRepository metricsRepository;
    private ActorRegistryClusterClient actorRegistryClient;
    private AgentRegistryClusterClient agentRegistryClient;
    private ResourceManagerClusterClient resourceManagerClient;
    private MetricsRepositoryClusterClient metricsClusterClient;
    private LeaderClusterClientFactory leaderClientFactory;
    private ClusterClientFactory clusterClientFactory;

    private BenchRule(@NotNull final Config leaderConfig, @NotNull final Config clusterConfig) {
        this.leaderConfig = checkNotNull(leaderConfig);
        this.clusterConfig = checkNotNull(clusterConfig);
    }

    public static BenchRule newJmsCluster() {
        JMSEndpoint endpoint = new JMSEndpoint(Network.LOCALHOST, Network.findFreePort());
        Config leaderConfig = ClusterConfigs.leaderJmsClusterConfig(endpoint, JMSLeaderClusterClientFactory.class);
        Config clusterConfig = ClusterConfigs.jmsClusterConfig(endpoint);
        return new BenchRule(leaderConfig, clusterConfig);
    }

    public static BenchRule newJgroupsCluster() {
        Config leaderConfig = ClusterConfigs.leaderJgroupsClusterConfig(JgroupsLeaderClusterClientFactory.class);
        Config clusterConfig = ClusterConfigs.jgroupsClusterConfig();
        return new BenchRule(leaderConfig, clusterConfig);
    }

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

    public MetricsRepository metricsRepository() {
        return metricsRepository;
    }

    @Override
    public void before() {
        log.debug("----- Initializing rule for {} -----", leaderConfig);
        actorRegistry = new ActorRegistry();
        leaderClientFactory = ClusterClients.newFactory(LeaderClusterClientFactory.class, leaderConfig, actorRegistry);
        actorRegistryClient = leaderClientFactory.createForActorRegistry();

        agentRegistry = new AgentRegistry();
        agentRegistryClient = leaderClientFactory.createForAgentRegistry(agentRegistry);

        resourceManagerClient = leaderClientFactory.createForResourceManager();
        resourceManager = new ResourceManager(resourceManagerClient, agentRegistry);

        ActorSender actorSender = leaderClientFactory.actorSender();

        clusterClientFactory = ClusterClients.newFactory(ClusterClientFactory.class,
                                                         clusterConfig,
                                                         new ActorRegistry());

        agents = new Agents(new ActorManagers(), clusterClientFactory, agentRegistry);
        actors = new Actors(actorSender, resourceManager, actorRegistry);

        metricsRepository = new MetricsRepository();
        metricsClusterClient = leaderClientFactory.createForMetricsRepository(metricsRepository);
        log.debug("----- Rule initialized for {} -----", leaderConfig);
    }

    @Override
    public void after() {
        log.debug("----- Closing rule for {} -----", leaderConfig);

        metricsClusterClient.close();
        resourceManagerClient.close();
        agentRegistryClient.close();
        actorRegistryClient.close();

        clusterClientFactory.close();
        leaderClientFactory.close();

        log.debug("----- Rule closed for {} -----", leaderConfig);
    }
}
