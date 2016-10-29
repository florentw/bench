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
package io.amaze.bench.leader.cluster;

import io.amaze.bench.runtime.actor.*;
import io.amaze.bench.runtime.agent.Agent;
import io.amaze.bench.runtime.agent.AgentKey;
import io.amaze.bench.runtime.agent.AgentRegistrationMessage;
import io.amaze.bench.runtime.cluster.registry.*;
import io.amaze.bench.shared.test.IntegrationTest;
import io.amaze.bench.util.BenchRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;
import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;
import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.runtime.actor.TestActor.DUMMY_JSON_CONFIG;
import static io.amaze.bench.runtime.cluster.registry.RegisteredActor.State;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Test interactions between the ResourceManager and an Agent
 */
@RunWith(Theories.class)
@Category(IntegrationTest.class)
public final class ResourceManagerAgentTest {

    @DataPoints
    public static final BenchRule[] benchRules = new BenchRule[]{ //
            BenchRule.newJmsCluster(), //
            BenchRule.newJgroupsCluster()};

    private static final int TEST_TIMEOUT_SEC = 15;

    @Rule
    public final Timeout globalTimeout = new Timeout(TEST_TIMEOUT_SEC * 2, TimeUnit.SECONDS);

    private ResourceManager resourceManager;

    private ActorRegistry actorRegistry;
    private AgentRegistry agentRegistry;

    private Agent agent;
    private AgentSync agentSync;

    @Theory
    public void agent_registration_complete(final BenchRule benchRule) throws Exception {
        before(benchRule);

        assertThat(agentRegistry.all().size(), is(1));
        RegisteredAgent registeredAgent = agentRegistry.all().iterator().next();
        assertThat(registeredAgent.getAgentKey(), is(agent.getKey()));
        assertTrue(registeredAgent.getCreationTime() > 0);
        assertNotNull(registeredAgent.getSystemConfig());
        after(benchRule);
    }

    @Theory
    public void create_embedded_actor_on_agent(final BenchRule benchRule) throws Exception {
        before(benchRule);

        List<String> preferredHosts = new ArrayList<>();
        ActorSync sync = createActorWith(preferredHosts);

        sync.assertActorCreated();
        sync.assertActorInitialized();
        assertThat(actorRegistry.all().size(), is(1));

        RegisteredActor actor = actorRegistry.byKey(DUMMY_ACTOR);
        assertThat(actor.getAgent(), is(agent.getKey()));
        assertThat(actor.getKey(), is(DUMMY_ACTOR));
        assertThat(actor.getState(), is(State.INITIALIZED));
        after(benchRule);
    }

    @Theory
    public void create_and_close_embedded_actor_on_agent(final BenchRule benchRule) throws Exception {
        before(benchRule);

        List<String> preferredHosts = new ArrayList<>();
        ActorSync sync = createActorWith(preferredHosts);
        sync.assertActorCreated();

        resourceManager.closeActor(DUMMY_ACTOR);

        sync.assertActorClosed();
        assertThat(actorRegistry.all().size(), is(0));
        after(benchRule);
    }

    @Theory
    public void closing_agent_unregisters(final BenchRule benchRule) throws Exception {
        before(benchRule);

        agent.close();

        assertTrue(agentSync.agentClosed.await(TEST_TIMEOUT_SEC, TimeUnit.SECONDS));
        after(benchRule);
    }

    private void before(final BenchRule benchRule) throws ExecutionException {
        benchRule.before();

        resourceManager = benchRule.resourceManager();

        actorRegistry = benchRule.actorRegistry();
        agentRegistry = benchRule.agentRegistry();
        agentSync = registerAgentSync();

        agent = getUninterruptibly(benchRule.agents().create(new AgentKey("test-agent")));
    }

    private void after(final BenchRule benchRule) throws Exception {
        resourceManager.close();
        agent.close();

        benchRule.after();
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
        return new DeployConfig(false, preferredHosts);
    }

    private AgentSync registerAgentSync() {
        AgentSync sync = new AgentSync();
        agentRegistry.addListener(sync);
        return sync;
    }

    private static final class AgentSync implements AgentRegistryListener {
        final CountDownLatch agentStarted = new CountDownLatch(1);
        final CountDownLatch agentClosed = new CountDownLatch(1);
        final CountDownLatch agentFailed = new CountDownLatch(1);

        @Override
        public void onAgentRegistration(@NotNull final AgentRegistrationMessage msg) {
            agentStarted.countDown();
        }

        @Override
        public void onAgentSignOff(@NotNull final AgentKey agent) {
            agentClosed.countDown();
        }

        @Override
        public void onAgentFailed(@NotNull final AgentKey agent, @NotNull final Throwable throwable) {
            agentFailed.countDown();
        }
    }

    private static final class ActorSync implements ActorRegistryListener {
        private final CountDownLatch actorCreated = new CountDownLatch(1);
        private final CountDownLatch actorInitialized = new CountDownLatch(1);
        private final CountDownLatch actorFailed = new CountDownLatch(1);
        private final CountDownLatch actorClosed = new CountDownLatch(1);

        @Override
        public void onActorCreated(@NotNull final ActorKey key, @NotNull final AgentKey agent) {
            actorCreated.countDown();
        }

        @Override
        public void onActorInitialized(@NotNull final ActorKey key, @NotNull final ActorDeployInfo deployInfo) {
            actorInitialized.countDown();
        }

        @Override
        public void onActorFailed(@NotNull final ActorKey key, @NotNull final Throwable throwable) {
            actorFailed.countDown();
        }

        @Override
        public void onActorClosed(@NotNull final ActorKey key) {
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
