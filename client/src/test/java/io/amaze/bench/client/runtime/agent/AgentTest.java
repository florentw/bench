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

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.client.runtime.actor.*;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.List;

import static io.amaze.bench.client.runtime.actor.ActorLifecycleMessage.Phase;
import static io.amaze.bench.client.runtime.actor.TestActor.*;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created on 3/3/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AgentTest {

    public static final String DUMMY_AGENT = "DUMMY_AGENT";

    private RecorderOrchestratorAgent agentClient;
    private RecorderOrchestratorActor actorClient;

    private ActorManager embeddedManager;
    @Mock
    private ActorManager forkedManager;
    @Mock
    private ActorManagers actorManagers;

    private Agent agent;
    private OrchestratorClientFactory clientFactory;

    @Before
    public void before() {
        agentClient = new RecorderOrchestratorAgent();
        actorClient = new RecorderOrchestratorActor();

        clientFactory = new DummyClientFactory(agentClient, actorClient);
        embeddedManager = spy(new EmbeddedActorManager(DUMMY_AGENT, new Actors(DUMMY_AGENT, clientFactory)));

        when(actorManagers.createEmbedded(anyString(),
                                          any(OrchestratorClientFactory.class))).thenReturn(embeddedManager);
        when(actorManagers.createForked(anyString())).thenReturn(forkedManager);

        agent = new Agent(clientFactory, actorManagers);
    }

    @After
    public void after() throws Exception {
        agent.close();
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(Agent.class);
        tester.testAllPublicInstanceMethods(agent);
    }

    @Test
    public void agent_is_created_properly() {
        // Smoke tests
        assertNotNull(agent);
        assertNotNull(agent.getName());

        // Check ActorManagers created
        verify(actorManagers).createEmbedded(agent.getName(), clientFactory);
        verify(actorManagers).createForked(agent.getName());
        verifyNoMoreInteractions(actorManagers);

        // Check listeners
        assertThat(agentClient.isAgentListenerStarted(), is(true));
    }

    @Test
    public void start_agent_registers_properly() throws Exception {
        assertThat(agentClient.getSentMessages().size(), is(1));
        assertThat(messagesToMaster().size(), is(1));
        assertTrue(firstMessage(messagesToMaster()) instanceof AgentRegistrationMessage);
    }

    @Test
    public void create_actor_registers_and_sends_lifecycle_msg() throws Exception {

        agent.onActorCreationRequest(DUMMY_CONFIG);

        assertThat(agent.getActors().size(), is(1));
        assertThat(agent.getActors().iterator().next().getName(), is(DUMMY_ACTOR));

        // Check managers interactions
        verify(embeddedManager).createActor(DUMMY_CONFIG);
        verifyZeroInteractions(forkedManager);

        // Check listeners
        assertThat(agentClient.isAgentListenerStarted(), is(true));
        assertThat(actorClient.isActorListenerStarted(), is(true));

        // Check good flow of messages
        assertThat(agentClient.getSentMessages().size(), is(1));
        assertThat(messagesToMaster().size(), is(2));

        // Check actor creation message
        ActorLifecycleMessage lfMsg = secondMessage(messagesToMaster());
        assertThat(lfMsg.getPhase(), is(Phase.CREATED));
    }

    @Test
    public void create_forked_actor_calls_the_right_manager() throws Exception {

        ActorConfig actorConfig = configForActor(TestActor.class, true);
        agent.onActorCreationRequest(actorConfig);

        // Check managers interactions
        verifyZeroInteractions(embeddedManager);
        verify(forkedManager).createActor(actorConfig);
    }

    @Test
    public void create_then_close_actor() throws Exception {

        agent.onActorCreationRequest(DUMMY_CONFIG);

        agent.onActorCloseRequest(DUMMY_ACTOR);

        // Check good flow of messages
        assertThat(agentClient.getSentMessages().size(), is(1));
        assertThat(messagesToMaster().size(), is(2));

        assertThat(actorClient.getSentMessages().size(), is(1));
        assertThat(messagesToActorMaster().size(), is(1));

        // Check closed message sent
        ActorLifecycleMessage lfMsg = firstMessage(messagesToActorMaster());
        assertThat(lfMsg.getPhase(), is(Phase.CLOSED));
    }

    @Test
    public void close_unknown_actor() throws Exception {

        agent.onActorCloseRequest(DUMMY_ACTOR);
        assertThat(agent.getActors().isEmpty(), is(true));

        // Check good flow of messages
        assertThat(agentClient.getSentMessages().size(), is(1));
        List<Message<? extends Serializable>> msgsToMaster = messagesToMaster();
        assertThat(msgsToMaster.size(), is(1));
    }

    @Test
    public void close_actor_failure() throws Exception {

        agent.onActorCreationRequest(configForActor(TestActorAfterThrows.class));

        // When
        agent.onActorCloseRequest(DUMMY_ACTOR);

        // Check good flow of messages
        assertThat(agentClient.getSentMessages().size(), is(1));
        assertThat(messagesToMaster().size(), is(2));

        assertThat(actorClient.getSentMessages().size(), is(1));
        assertThat(messagesToActorMaster().size(), is(1));

        // Check failed message sent
        ActorLifecycleMessage lfMsg = firstMessage(messagesToActorMaster());
        assertThat(lfMsg.getPhase(), is(Phase.FAILED));
    }

    @Test
    public void closing_agent_closes_actors_and_unregisters() throws Exception {
        //Given
        agent.onActorCreationRequest(DUMMY_CONFIG);

        // When
        agent.close();

        // Then
        assertThat(agent.getActors().size(), is(0));

        // Check good flow of messages
        assertThat(agentClient.getSentMessages().size(), is(1));
        assertThat(messagesToMaster().size(), is(3));

        assertThat(actorClient.getSentMessages().size(), is(1));
        assertThat(messagesToActorMaster().size(), is(1));

        // Check actor is closed
        ActorLifecycleMessage lfMsg = firstMessage(messagesToActorMaster());
        assertThat(lfMsg.getPhase(), is(Phase.CLOSED));

        // Check sign off message
        String lastMsg = thirdMessage(messagesToMaster());
        assertTrue(lastMsg.equals(agent.getName()));
    }

    @Test
    public void create_same_actor_twice_registers_only_one() throws Exception {

        agent.onActorCreationRequest(DUMMY_CONFIG);
        agent.onActorCreationRequest(DUMMY_CONFIG);
        assertThat(agent.getActors().size(), is(1));

        // Check good flow of messages
        assertThat(agentClient.getSentMessages().size(), is(1));
        assertThat(messagesToMaster().size(), is(2));
    }

    @Test
    public void create_invalid_actor() throws Exception {

        agent.onActorCreationRequest(configForActor(String.class));
        assertThat(agent.getActors().isEmpty(), is(true));

        // Check good flow of messages
        assertThat(agentClient.getSentMessages().size(), is(1));
        assertThat(messagesToMaster().size(), is(2));

        // Check failed message sent
        ActorLifecycleMessage lfMsg = secondMessage(messagesToMaster());
        assertThat(lfMsg.getPhase(), is(Phase.FAILED));
        assertNotNull(lfMsg.getThrowable());
        assertThat(lfMsg.getActor(), is(DUMMY_ACTOR));
    }

    private List<Message<? extends Serializable>> messagesToActorMaster() {
        return actorClient.getSentMessages().get(Constants.MASTER_ACTOR_NAME);
    }

    private List<Message<? extends Serializable>> messagesToMaster() {
        return agentClient.getSentMessages().get(Constants.MASTER_ACTOR_NAME);
    }

    private <T extends Serializable> T firstMessage(final List<Message<? extends Serializable>> messageList) {
        return (T) ((AgentOutputMessage) messageList.get(0).data()).getData();
    }

    private <T extends Serializable> T secondMessage(final List<Message<? extends Serializable>> messageList) {
        return (T) ((AgentOutputMessage) messageList.get(1).data()).getData();
    }

    private <T extends Serializable> T thirdMessage(final List<Message<? extends Serializable>> messageList) {
        return (T) ((AgentOutputMessage) messageList.get(2).data()).getData();
    }

}