package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.runtime.cluster.ActorClusterClient;
import io.amaze.bench.runtime.cluster.AgentClusterClient;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryClusterClient;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Created on 10/5/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsClusterMemberClientFactoryTest {

    @Mock
    private JChannel jChannel;
    @Mock
    private ActorRegistry actorRegistry;
    @Mock
    private Address address;

    private JgroupsClusterClientFactory clusterClientFactory;

    @Before
    public void init() {
        when(jChannel.getAddress()).thenReturn(address);
        clusterClientFactory = new JgroupsClusterClientFactory(jChannel, actorRegistry);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(JgroupsClusterClientFactory.class);
        tester.testAllPublicInstanceMethods(clusterClientFactory);
    }

    @Test
    public void create_for_actor_returns_instance() {
        ActorClusterClient actorClient = clusterClientFactory.createForActor(DUMMY_ACTOR);

        assertNotNull(actorClient);
    }

    @Test
    public void create_for_agent_returns_instance() {
        AgentClusterClient agentClient = clusterClientFactory.createForAgent("agent");

        assertNotNull(agentClient);
    }

    @Test
    public void create_for_actor_registry_returns_instance() {
        ActorRegistryClusterClient actorRegistryClient = clusterClientFactory.createForActorRegistry();

        assertNotNull(actorRegistryClient);
    }
}