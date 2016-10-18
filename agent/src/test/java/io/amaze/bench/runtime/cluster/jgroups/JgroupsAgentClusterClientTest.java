package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.runtime.actor.ActorInputMessage;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.runtime.agent.AgentClientListener;
import io.amaze.bench.runtime.agent.AgentInputMessage;
import io.amaze.bench.runtime.cluster.ActorCreationRequest;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.shared.jgroups.JgroupsListenerMultiplexer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 10/14/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsAgentClusterClientTest {

    private static final String DUMMY_AGENT = "agent";

    @Mock
    private JgroupsListenerMultiplexer listenerMultiplexer;
    @Mock
    private JgroupsSender jgroupsSender;
    @Mock
    private AgentClientListener agentClientListener;
    @Mock
    private ActorRegistry actorRegistry;

    private JgroupsAgentClusterClient clusterClient;

    @Before
    public void init() {
        clusterClient = new JgroupsAgentClusterClient(listenerMultiplexer, jgroupsSender, actorRegistry);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorInputMessage.class, ActorInputMessage.init());
        tester.setDefault(ActorKey.class, DUMMY_ACTOR);

        tester.testAllPublicConstructors(JgroupsAgentClusterClient.class);
        tester.testAllPublicInstanceMethods(clusterClient);
    }

    @Test
    public void starting_agent_listener_registers_it() {

        clusterClient.startAgentListener(DUMMY_AGENT, agentClientListener);

        verify(listenerMultiplexer).addListener(eq(AgentInputMessage.class),
                                                any(JgroupsAgentClusterClient.MessageListener.class));
        verifyNoMoreInteractions(listenerMultiplexer);
        verifyZeroInteractions(jgroupsSender);
    }

    @Test
    public void close_unregisters_listener() {

        clusterClient.close();

        verify(listenerMultiplexer).removeListenerFor(AgentInputMessage.class);
        verifyNoMoreInteractions(listenerMultiplexer);
        verifyZeroInteractions(jgroupsSender);
    }

    @Test
    public void message_listener_forwards_createActor() {
        JgroupsAgentClusterClient.MessageListener messageListener = new JgroupsAgentClusterClient.MessageListener(
                DUMMY_AGENT,
                agentClientListener);

        messageListener.onMessage(mock(org.jgroups.Message.class),
                                  AgentInputMessage.createActor(DUMMY_AGENT,
                                                                new ActorCreationRequest(TestActor.DUMMY_CONFIG)));

        verify(agentClientListener).onActorCreationRequest(TestActor.DUMMY_CONFIG);
        verifyNoMoreInteractions(agentClientListener);
    }

    @Test
    public void message_listener_forwards_closeActor() {
        JgroupsAgentClusterClient.MessageListener messageListener = new JgroupsAgentClusterClient.MessageListener(
                DUMMY_AGENT,
                agentClientListener);

        messageListener.onMessage(mock(org.jgroups.Message.class),
                                  AgentInputMessage.closeActor(DUMMY_AGENT, DUMMY_ACTOR));

        verify(agentClientListener).onActorCloseRequest(DUMMY_ACTOR);
        verifyNoMoreInteractions(agentClientListener);
    }

}