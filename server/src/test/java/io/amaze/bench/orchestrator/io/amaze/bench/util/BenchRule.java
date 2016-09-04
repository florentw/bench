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
package io.amaze.bench.orchestrator.io.amaze.bench.util;

import io.amaze.bench.client.runtime.actor.ActorManagers;
import io.amaze.bench.client.runtime.agent.Agent;
import io.amaze.bench.client.runtime.orchestrator.JMSOrchestratorClientFactory;
import io.amaze.bench.orchestrator.JMSOrchestratorServer;
import io.amaze.bench.orchestrator.ResourceManager;
import io.amaze.bench.orchestrator.registry.ActorRegistry;
import io.amaze.bench.orchestrator.registry.AgentRegistry;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.test.JMSServerRule;
import org.junit.rules.ExternalResource;

/**
 * JUnit 4 rule that allows to instantiate a complete ecosystem to run integration tests.<br/>
 * Created on 9/1/16.
 */
public final class BenchRule extends ExternalResource {

    private final JMSServerRule jmsServerRule;

    private AgentRegistry agentRegistry;
    private ActorRegistry actorRegistry;
    private ResourceManager resourceManager;

    private JMSClient orchestratorServerJmsClient;
    private JMSOrchestratorServer orchestratorServer;

    private JMSOrchestratorClientFactory orchestratorClientFactory;

    public BenchRule() {
        this.jmsServerRule = new JMSServerRule();
    }

    public Agent createAgent() {
        return new Agent(orchestratorClientFactory, new ActorManagers());
    }

    public JMSServerRule getJmsServerRule() {
        return jmsServerRule;
    }

    public JMSOrchestratorServer getOrchestratorServer() {
        return orchestratorServer;
    }

    public AgentRegistry getAgentRegistry() {
        return agentRegistry;
    }

    public ActorRegistry getActorRegistry() {
        return actorRegistry;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public JMSClient createClient() {
        return jmsServerRule.createClient();
    }

    @Override
    protected void before() throws Throwable {
        jmsServerRule.init();

        orchestratorServerJmsClient = createClient();
        orchestratorServer = new JMSOrchestratorServer(jmsServerRule.getServer(), orchestratorServerJmsClient);

        agentRegistry = new AgentRegistry();
        actorRegistry = new ActorRegistry();

        orchestratorServer.startRegistryListeners(agentRegistry.getListenerForOrchestrator(),
                                                  actorRegistry.getListenerForOrchestrator());

        resourceManager = new ResourceManager(orchestratorServer, agentRegistry);

        orchestratorClientFactory = new JMSOrchestratorClientFactory(jmsServerRule.getEndpoint());
    }

    @Override
    protected void after() {
        orchestratorServerJmsClient.close();

        jmsServerRule.close();
    }
}
