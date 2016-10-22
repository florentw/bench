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
import io.amaze.bench.leader.cluster.ResourceManager;
import io.amaze.bench.leader.cluster.ResourceManagerClusterClient;
import io.amaze.bench.leader.cluster.jms.JMSMetricsRepositoryClusterClient;
import io.amaze.bench.leader.cluster.jms.JMSResourceManagerClusterClient;
import io.amaze.bench.leader.cluster.registry.MetricsRepository;
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
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSServerRule;
import org.junit.rules.ExternalResource;

/**
 * JUnit 4 rule that allows to instantiate a complete ecosystem to run integration tests.
 */
public final class BenchRule extends ExternalResource {

    private final JMSServerRule jmsServerRule;

    private AgentRegistry agentRegistry;
    private ActorRegistry actorRegistry;
    private ResourceManager resourceManager;
    private ActorSender actorSender;

    private JMSClient resourceManagerJmsClient;
    private JMSClient metricsRepositoryClient;

    private Agents agents;
    private Actors actors;
    private MetricsRepository metricsRepository;
    private ActorRegistryClusterClient actorRegistryClient;
    private AgentRegistryClusterClient agentRegistryClient;

    public BenchRule() {
        this.jmsServerRule = new JMSServerRule();
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

    public ActorSender actorSender() {
        return actorSender;
    }

    public MetricsRepository metricsRepository() {
        return metricsRepository;
    }

    @Override
    protected void before() {
        jmsServerRule.init();

        JMSEndpoint endpoint = jmsServerRule.getEndpoint();
        Config clusterConfig = ClusterConfigs.jmsClusterConfig(endpoint);

        actorRegistry = new ActorRegistry();
        ClusterClientFactory ruleFactory = ClusterClients.newFactory(clusterConfig, actorRegistry);

        actorRegistryClient = ruleFactory.createForActorRegistry();

        agentRegistry = new AgentRegistry();
        agentRegistryClient = ruleFactory.createForAgentRegistry(agentRegistry);

        resourceManagerJmsClient = createClient();
        ResourceManagerClusterClient resourceManagerClient = new JMSResourceManagerClusterClient(jmsServerRule.getServer(),
                                                                                                 resourceManagerJmsClient);
        resourceManager = new ResourceManager(resourceManagerClient, agentRegistry);

        ActorClusterClient clusterClient = ruleFactory.createForActor(new ActorKey("system-test"));
        actorSender = clusterClient.actorSender();

        ClusterClientFactory clusterClientFactory = ClusterClients.newFactory(clusterConfig, new ActorRegistry());

        agents = new Agents(new ActorManagers(), clusterClientFactory, agentRegistry);
        actors = new Actors(actorSender, resourceManager, actorRegistry);

        initMetrics();
    }

    @Override
    protected void after() {
        resourceManagerJmsClient.close();
        metricsRepositoryClient.close();
        actorRegistryClient.close();
        agentRegistryClient.close();

        jmsServerRule.close();
    }

    private JMSClient createClient() {
        return jmsServerRule.createClient();
    }

    private void initMetrics() {
        metricsRepository = new MetricsRepository();
        metricsRepositoryClient = createClient();
        JMSMetricsRepositoryClusterClient jmsMetricsClusterClient = new JMSMetricsRepositoryClusterClient(
                metricsRepositoryClient);
        jmsMetricsClusterClient.startMetricsListener(metricsRepository.createClusterListener());
    }
}
