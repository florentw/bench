package io.amaze.bench.client.runtime.agent;

import io.amaze.bench.client.runtime.actor.*;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClient;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;
import org.junit.Before;
import org.junit.Test;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.amaze.bench.client.runtime.actor.ActorLifecycleMessage.Phase;
import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_CONFIG;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created on 3/3/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class AgentTest {

    private RecorderOrchestratorClient agentClient;
    private RecorderOrchestratorClient actorClient;
    private DummyClientFactory clientFactory;
    private ActorManager manager;

    @Before
    public void before() {
        agentClient = new RecorderOrchestratorClient();
        actorClient = new RecorderOrchestratorClient();

        clientFactory = new DummyClientFactory(agentClient, actorClient);
        manager = new EmbeddedActorManager(new ActorFactory(clientFactory));
    }

    @Test
    public void start_agent_registers_properly() throws Exception {
        try (Agent agent = new Agent(clientFactory, manager)) {
            assertNotNull(agent);

            // Check listeners
            assertThat(agentClient.isAgentListenerStarted(), is(true));
            assertThat(agentClient.isActorListenerStarted(), is(false));

            // Check actor registration message
            assertThat(agentClient.getSentMessages().size(), is(1));
            List<Message<? extends Serializable>> msgsToMaster = agentClient.getSentMessages().get(Constants.MASTER_ACTOR_NAME);
            assertThat(msgsToMaster.size(), is(1));
            assertTrue(((MasterOutputMessage) msgsToMaster.get(0).data()).getData() instanceof AgentRegistrationMessage);
        }
    }

    @Test
    public void create_actor_registers_and_sends_lifecycle_msg() throws Exception {
        try (Agent agent = new Agent(clientFactory, manager)) {

            agent.onActorCreationRequest(DUMMY_ACTOR, TestActor.class.getCanonicalName(), DUMMY_CONFIG);

            assertThat(agent.getActors().size(), is(1));
            assertThat(agent.getActors().iterator().next().name(), is(DUMMY_ACTOR));

            // Check listeners
            assertThat(agentClient.isAgentListenerStarted(), is(true));
            assertThat(actorClient.isActorListenerStarted(), is(true));

            // Check good flow of messages
            assertThat(agentClient.getSentMessages().size(), is(1));
            List<Message<? extends Serializable>> msgsToMaster = agentClient.getSentMessages().get(Constants.MASTER_ACTOR_NAME);
            assertThat(msgsToMaster.size(), is(2));

            // Check actor creation message
            ActorLifecycleMessage lfMsg = (ActorLifecycleMessage) ((MasterOutputMessage) msgsToMaster.get(1).data()).getData();
            assertThat(lfMsg.getPhase(), is(Phase.CREATED));
        }
    }

    @Test
    public void create_then_close_actor() throws Exception {
        try (Agent agent = new Agent(clientFactory, manager)) {
            agent.onActorCreationRequest(DUMMY_ACTOR, TestActor.class.getCanonicalName(), DUMMY_CONFIG);

            agent.onActorCloseRequest(DUMMY_ACTOR);

            // Check good flow of messages
            assertThat(agentClient.getSentMessages().size(), is(1));
            List<Message<? extends Serializable>> msgsToMaster = agentClient.getSentMessages().get(Constants.MASTER_ACTOR_NAME);
            assertThat(msgsToMaster.size(), is(2));

            assertThat(actorClient.getSentMessages().size(), is(1));
            List<Message<? extends Serializable>> actorMsgsToMaster = actorClient.getSentMessages().get(Constants.MASTER_ACTOR_NAME);
            assertThat(actorMsgsToMaster.size(), is(1));

            // Check closed message sent
            ActorLifecycleMessage lfMsg = (ActorLifecycleMessage) ((MasterOutputMessage) actorMsgsToMaster.get(0).data()).getData();
            assertThat(lfMsg.getPhase(), is(Phase.CLOSED));
        }
    }

    @Test
    public void close_unknown_actor() throws Exception {
        try (Agent agent = new Agent(clientFactory, manager)) {
            agent.onActorCloseRequest(DUMMY_ACTOR);
            assertThat(agent.getActors().isEmpty(), is(true));

            // Check good flow of messages
            assertThat(agentClient.getSentMessages().size(), is(1));
            List<Message<? extends Serializable>> msgsToMaster = agentClient.getSentMessages().get(Constants.MASTER_ACTOR_NAME);
            assertThat(msgsToMaster.size(), is(1));
        }
    }

    @Test
    public void close_actor_failure() throws Exception {
        try (Agent agent = new Agent(clientFactory, manager)) {
            agent.onActorCreationRequest(DUMMY_ACTOR, TestActorAfterThrows.class.getCanonicalName(), DUMMY_CONFIG);

            agent.onActorCloseRequest(DUMMY_ACTOR);

            // Check good flow of messages
            assertThat(agentClient.getSentMessages().size(), is(1));
            List<Message<? extends Serializable>> msgsToMaster = agentClient.getSentMessages().get(Constants.MASTER_ACTOR_NAME);
            assertThat(msgsToMaster.size(), is(2));

            assertThat(actorClient.getSentMessages().size(), is(1));
            List<Message<? extends Serializable>> actorMsgsToMaster = actorClient.getSentMessages().get(Constants.MASTER_ACTOR_NAME);
            assertThat(actorMsgsToMaster.size(), is(1));

            // Check failed message sent
            ActorLifecycleMessage lfMsg = (ActorLifecycleMessage) ((MasterOutputMessage) actorMsgsToMaster.get(0).data()).getData();
            assertThat(lfMsg.getPhase(), is(Phase.FAILED));
        }
    }

    @Test
    public void closing_agent_closes_actors_and_unregisters() throws Exception {
        Agent outAgent;
        try (Agent agent = new Agent(clientFactory, manager)) {
            agent.onActorCreationRequest(DUMMY_ACTOR, TestActor.class.getCanonicalName(), DUMMY_CONFIG);
            outAgent = agent;
        }

        assertThat(outAgent.getActors().size(), is(0));

        // Check good flow of messages
        assertThat(agentClient.getSentMessages().size(), is(1));
        List<Message<? extends Serializable>> msgsToMaster = agentClient.getSentMessages().get(Constants.MASTER_ACTOR_NAME);
        assertThat(msgsToMaster.size(), is(3));

        assertThat(actorClient.getSentMessages().size(), is(1));
        List<Message<? extends Serializable>> actorMsgsToMaster = actorClient.getSentMessages().get(Constants.MASTER_ACTOR_NAME);
        assertThat(actorMsgsToMaster.size(), is(1));

        // Check actor is closed
        ActorLifecycleMessage lfMsg = (ActorLifecycleMessage) ((MasterOutputMessage) actorMsgsToMaster.get(0).data()).getData();
        assertThat(lfMsg.getPhase(), is(Phase.CLOSED));

        // Check sign off message
        Serializable lastMsg = ((MasterOutputMessage) msgsToMaster.get(2).data()).getData();
        assertTrue(lastMsg.equals(AgentSignOffMessage.create()));
    }

    @Test
    public void create_same_actor_twice_registers_only_one() throws Exception {
        try (Agent agent = new Agent(clientFactory, manager)) {
            agent.onActorCreationRequest(DUMMY_ACTOR, TestActor.class.getCanonicalName(), DUMMY_CONFIG);
            agent.onActorCreationRequest(DUMMY_ACTOR, TestActor.class.getCanonicalName(), DUMMY_CONFIG);
            assertThat(agent.getActors().size(), is(1));

            // Check good flow of messages
            assertThat(agentClient.getSentMessages().size(), is(1));
            List<Message<? extends Serializable>> msgsToMaster = agentClient.getSentMessages().get(Constants.MASTER_ACTOR_NAME);
            assertThat(msgsToMaster.size(), is(2));
        }
    }

    @Test
    public void create_invalid_actor() throws Exception {
        try (Agent agent = new Agent(clientFactory, manager)) {
            agent.onActorCreationRequest(DUMMY_ACTOR, String.class.getCanonicalName(), DUMMY_CONFIG);
            assertThat(agent.getActors().isEmpty(), is(true));

            // Check good flow of messages
            assertThat(agentClient.getSentMessages().size(), is(1));
            List<Message<? extends Serializable>> msgsToMaster = agentClient.getSentMessages().get(Constants.MASTER_ACTOR_NAME);
            assertThat(msgsToMaster.size(), is(2));

            // Check failed message sent
            ActorLifecycleMessage lfMsg = (ActorLifecycleMessage) ((MasterOutputMessage) msgsToMaster.get(1).data()).getData();
            assertThat(lfMsg.getPhase(), is(Phase.FAILED));
            assertNotNull(lfMsg.getThrowable());
            assertThat(lfMsg.getActor(), is(DUMMY_ACTOR));
        }
    }

    public static class RecorderOrchestratorClient implements OrchestratorClient {

        private final Map<String, List<Message<? extends Serializable>>> sentMessages = new HashMap<>();

        private boolean agentListenerStarted = false;
        private boolean actorListenerStarted = false;

        @Override
        public void startAgentListener(@NotNull final String agent,
                                       @NotNull final String agentsTopic,
                                       @NotNull final AgentClientListener listener) {
            agentListenerStarted = true;
        }

        @Override
        public void startActorListener(@NotNull final Actor actor) {
            actorListenerStarted = true;
        }

        @Override
        public void sendToActor(@NotNull final String to, @NotNull final Message<? extends Serializable> message) {
            List<Message<? extends Serializable>> msgs = sentMessages.get(to);
            if (msgs == null) {
                msgs = new ArrayList<>();
                sentMessages.put(to, msgs);
            }
            msgs.add(message);
        }

        @Override
        public void close() {
            // Dummy
        }

        boolean isAgentListenerStarted() {
            return agentListenerStarted;
        }

        boolean isActorListenerStarted() {
            return actorListenerStarted;
        }

        public Map<String, List<Message<? extends Serializable>>> getSentMessages() {
            return sentMessages;
        }
    }

    public static final class DummyClientFactory implements OrchestratorClientFactory {

        private final OrchestratorClient agentClient;
        private final OrchestratorClient actorClient;

        public DummyClientFactory(final OrchestratorClient agentClient, final OrchestratorClient actorClient) {
            this.agentClient = agentClient;
            this.actorClient = actorClient;
        }

        @Override
        public OrchestratorClient createForAgent() {
            return agentClient;
        }

        @Override
        public OrchestratorClient createForActor() {
            return actorClient;
        }
    }
}