package io.amaze.bench.orchestrator;

import io.amaze.bench.client.runtime.actor.ActorConfig;
import io.amaze.bench.client.runtime.actor.ActorManagers;
import io.amaze.bench.client.runtime.actor.DeployConfig;
import io.amaze.bench.client.runtime.actor.TestActor;
import io.amaze.bench.client.runtime.agent.Agent;
import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import io.amaze.bench.client.runtime.orchestrator.JMSOrchestratorClientFactory;
import io.amaze.bench.orchestrator.registry.*;
import io.amaze.bench.orchestrator.registry.RegisteredActor.State;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSServer;
import io.amaze.bench.shared.test.IntegrationTest;
import io.amaze.bench.shared.test.JMSServerRule;
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

import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_JSON_CONFIG;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created on 4/5/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
@Category(IntegrationTest.class)
public final class ResourceManagerAgentTest {

    @Rule
    public final JMSServerRule serverRule = new JMSServerRule();

    private JMSClient jmsClient;

    private JMSOrchestratorServer orchestratorServer;
    private ResourceManagerImpl resourceManager;

    private Agent agent;
    private ActorRegistry actorRegistry;
    private AgentRegistry agentRegistry;
    private AgentSync agentSync;

    @Before
    public void before() throws InterruptedException {
        jmsClient = serverRule.createClient();
        JMSServer jmsServer = serverRule.getServer();

        orchestratorServer = new JMSOrchestratorServer(jmsServer, jmsClient);
        agentRegistry = new AgentRegistry();

        actorRegistry = new ActorRegistry();
        agentSync = getAgentSync();

        orchestratorServer.startRegistryListeners(agentRegistry.getListenerForOrchestrator(),
                                                  actorRegistry.getListenerForOrchestrator());

        resourceManager = new ResourceManagerImpl(orchestratorServer, agentRegistry);

        JMSOrchestratorClientFactory factory = new JMSOrchestratorClientFactory(serverRule.getHost(),
                                                                                serverRule.getPort());
        agent = new Agent(factory, new ActorManagers());

        agentSync.agentStarted.await();
    }

    @Test
    public void agent_registration_complete() throws InterruptedException {
        assertThat(agentRegistry.all().size(), is(1));
        RegisteredAgent registeredAgent = agentRegistry.all().iterator().next();
        assertThat(registeredAgent.getName(), is(agent.getName()));
        assertTrue(registeredAgent.getCreationTime() > 0);
        assertNotNull(registeredAgent.getSystemInfo());
    }

    @Test
    public void create_embedded_actor_on_agent() throws InterruptedException {
        List<String> preferredHosts = new ArrayList<>();
        ActorSync sync = getActorSync();
        ActorConfig actorConfig = new ActorConfig(DUMMY_ACTOR,
                                                  TestActor.class.getName(),
                                                  new DeployConfig(serverRule.getHost(),
                                                                   serverRule.getPort(),
                                                                   false,
                                                                   preferredHosts),
                                                  DUMMY_JSON_CONFIG);

        resourceManager.createActor(actorConfig);

        assertTrue(sync.actorCreated.await(2, TimeUnit.SECONDS));
        assertThat(actorRegistry.all().size(), is(1));

        RegisteredActor actor = actorRegistry.byName(DUMMY_ACTOR);
        assertThat(actor.getAgent(), is(agent.getName()));
        assertThat(actor.getName(), is(DUMMY_ACTOR));
        assertThat(actor.getState(), is(State.CREATED));
    }

    @Test
    public void create_and_stop_embedded_actor_on_agent() throws InterruptedException {
        // Given
        List<String> preferredHosts = new ArrayList<>();
        ActorSync sync = getActorSync();
        ActorConfig actorConfig = new ActorConfig(DUMMY_ACTOR,
                                                  TestActor.class.getName(),
                                                  new DeployConfig(serverRule.getHost(),
                                                                   serverRule.getPort(),
                                                                   false,
                                                                   preferredHosts),
                                                  DUMMY_JSON_CONFIG);

        resourceManager.createActor(actorConfig);
        assertTrue(sync.actorCreated.await(2, TimeUnit.SECONDS));

        // When
        resourceManager.closeActor(DUMMY_ACTOR);

        // Then
        assertTrue(sync.actorClosed.await(2, TimeUnit.SECONDS));
        assertThat(actorRegistry.all().size(), is(0));
    }

    @Test
    public void closing_agent_unregisters() throws Exception {
        agent.close();

        assertTrue(agentSync.agentClosed.await(2, TimeUnit.SECONDS));
    }

    @After
    public void after() throws Exception {
        resourceManager.close();
        agent.close();
        jmsClient.close();
        orchestratorServer.close();
    }

    private AgentSync getAgentSync() {
        AgentSync sync = new AgentSync();
        agentRegistry.addListener(sync);
        return sync;
    }

    private ActorSync getActorSync() {
        ActorSync sync = new ActorSync();
        actorRegistry.addListener(sync);
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
        private final CountDownLatch actorStarted = new CountDownLatch(1);
        private final CountDownLatch actorFailed = new CountDownLatch(1);
        private final CountDownLatch actorClosed = new CountDownLatch(1);

        @Override
        public void onActorCreated(@NotNull final String name, @NotNull final String agent) {
            actorCreated.countDown();
        }

        @Override
        public void onActorStarted(@NotNull final String name, @NotNull final String agent) {
            actorStarted.countDown();
        }

        @Override
        public void onActorFailed(@NotNull final String name, @NotNull final Throwable throwable) {
            actorFailed.countDown();
        }

        @Override
        public void onActorClosed(@NotNull final String name) {
            actorClosed.countDown();
        }
    }

}
