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
package io.amaze.bench.orchestrator;

import io.amaze.bench.client.runtime.actor.ActorConfig;
import io.amaze.bench.client.runtime.actor.ActorInputMessage;
import io.amaze.bench.client.runtime.actor.DeployConfig;
import io.amaze.bench.client.runtime.actor.TestActor;
import io.amaze.bench.client.runtime.agent.Agent;
import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import io.amaze.bench.orchestrator.io.amaze.bench.util.BenchRule;
import io.amaze.bench.orchestrator.registry.*;
import io.amaze.bench.orchestrator.registry.RegisteredActor.State;
import io.amaze.bench.shared.test.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;
import static io.amaze.bench.client.runtime.actor.ActorInputMessage.Command;
import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_JSON_CONFIG;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created on 4/5/16.
 */
@Category(IntegrationTest.class)
public final class ResourceManagerAgentTest {

    private static final int TEST_TIMEOUT_SEC = 5;

    @Rule
    public final BenchRule benchRule = new BenchRule();

    private JMSOrchestratorServer orchestratorServer;
    private ResourceManager resourceManager;

    private ActorRegistry actorRegistry;
    private AgentRegistry agentRegistry;

    private Agent agent;
    private AgentSync agentSync;

    @Before
    public void before() {
        orchestratorServer = benchRule.getOrchestratorServer();
        resourceManager = benchRule.getResourceManager();

        actorRegistry = benchRule.getActorRegistry();
        agentRegistry = benchRule.getAgentRegistry();
        agentSync = registerAgentSync();

        agent = benchRule.createAgent();
        awaitUninterruptibly(agentSync.agentStarted);
    }

    @Test
    public void agent_registration_complete() throws InterruptedException {
        assertThat(agentRegistry.all().size(), is(1));
        RegisteredAgent registeredAgent = agentRegistry.all().iterator().next();
        assertThat(registeredAgent.getName(), is(agent.getName()));
        assertTrue(registeredAgent.getCreationTime() > 0);
        assertNotNull(registeredAgent.getSystemConfig());
    }

    @Test
    public void create_embedded_actor_on_agent() throws InterruptedException {
        List<String> preferredHosts = new ArrayList<>();
        ActorSync sync = createActorWith(preferredHosts);

        sync.assertActorCreated();
        assertThat(actorRegistry.all().size(), is(1));

        RegisteredActor actor = actorRegistry.byName(DUMMY_ACTOR);
        assertThat(actor.getAgent(), is(agent.getName()));
        assertThat(actor.getName(), is(DUMMY_ACTOR));
        assertThat(actor.getState(), is(State.CREATED));
    }

    @Test
    public void init_embedded_actor_on_agent() throws InterruptedException {
        List<String> preferredHosts = new ArrayList<>();
        ActorSync sync = createActorWith(preferredHosts);
        sync.assertActorCreated();

        ActorInputMessage initMessage = new ActorInputMessage(Command.INIT, //
                                                              agent.getName(), //
                                                              "");
        orchestratorServer.sendToActor(DUMMY_ACTOR, initMessage);

        sync.assertActorInitialized();

        RegisteredActor actor = actorRegistry.byName(DUMMY_ACTOR);
        assertThat(actor.getAgent(), is(agent.getName()));
        assertThat(actor.getName(), is(DUMMY_ACTOR));
        assertThat(actor.getState(), is(State.INITIALIZED));
    }

    @Test
    public void create_and_close_embedded_actor_on_agent() throws InterruptedException {
        // Given
        List<String> preferredHosts = new ArrayList<>();
        ActorSync sync = createActorWith(preferredHosts);
        sync.assertActorCreated();

        // When
        resourceManager.closeActor(DUMMY_ACTOR);

        // Then
        sync.assertActorClosed();
        assertThat(actorRegistry.all().size(), is(0));
    }

    @Test
    public void closing_agent_unregisters() throws Exception {
        agent.close();

        assertTrue(agentSync.agentClosed.await(TEST_TIMEOUT_SEC, TimeUnit.SECONDS));
    }

    @After
    public void after() throws Exception {
        resourceManager.close();
        agent.close();
        orchestratorServer.close();
    }

    private ActorSync createActorWith(final List<String> preferredHosts) {
        ActorSync sync = new ActorSync();
        actorRegistry.addListener(sync);
        ActorConfig actorConfig = new ActorConfig(DUMMY_ACTOR,
                                                  TestActor.class.getName(),
                                                  deployConfig(preferredHosts),
                                                  DUMMY_JSON_CONFIG);

        resourceManager.createActor(actorConfig);
        return sync;
    }

    private DeployConfig deployConfig(final List<String> preferredHosts) {
        return new DeployConfig(benchRule.getJmsServerRule().getEndpoint(),
                                false,
                                preferredHosts);
    }

    private AgentSync registerAgentSync() {
        AgentSync sync = new AgentSync();
        agentRegistry.addListener(sync);
        return sync;
    }

    private static final class AgentSync implements AgentRegistryListener {
        final CountDownLatch agentStarted = new CountDownLatch(1);
        final CountDownLatch agentClosed = new CountDownLatch(1);

        @Override
        public void onAgentRegistration(@NotNull final AgentRegistrationMessage msg) {
            agentStarted.countDown();
        }

        @Override
        public void onAgentSignOff(@NotNull final String agent) {
            agentClosed.countDown();
        }
    }

    private static final class ActorSync implements ActorRegistryListener {
        private final CountDownLatch actorCreated = new CountDownLatch(1);
        private final CountDownLatch actorInitialized = new CountDownLatch(1);
        private final CountDownLatch actorFailed = new CountDownLatch(1);
        private final CountDownLatch actorClosed = new CountDownLatch(1);

        @Override
        public void onActorCreated(@NotNull final String name, @NotNull final String agent) {
            actorCreated.countDown();
        }

        @Override
        public void onActorInitialized(@NotNull final String name, @NotNull final String agent) {
            actorInitialized.countDown();
        }

        @Override
        public void onActorFailed(@NotNull final String name, @NotNull final Throwable throwable) {
            actorFailed.countDown();
        }

        @Override
        public void onActorClosed(@NotNull final String name) {
            actorClosed.countDown();
        }

        void assertActorInitialized() throws InterruptedException {
            assertTrue(awaitUninterruptibly(actorInitialized, TEST_TIMEOUT_SEC, TimeUnit.SECONDS));
        }

        void assertActorCreated() throws InterruptedException {
            assertTrue(awaitUninterruptibly(actorCreated, TEST_TIMEOUT_SEC, TimeUnit.SECONDS));
        }

        void assertActorClosed() throws InterruptedException {
            assertTrue(awaitUninterruptibly(actorClosed, TEST_TIMEOUT_SEC, TimeUnit.SECONDS));
        }
    }

}
