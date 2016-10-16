package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.runtime.cluster.ActorClusterClient;
import io.amaze.bench.runtime.cluster.AgentClusterClient;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryListener;
import io.amaze.bench.util.ClusterConfigs;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.ViewId;
import org.jgroups.stack.IpAddress;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.UnknownHostException;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created on 10/5/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsClusterClientFactoryTest {

    @Mock
    private ActorRegistry actorRegistry;
    @Mock
    private Address address;
    @Mock
    private ActorRegistryListener registryClusterListener;

    private JgroupsClusterClientFactory clusterClientFactory;

    @Before
    public void init() throws UnknownHostException {
        View initialView = new View(new ViewId(), new Address[]{new IpAddress("localhost", 1337)});
        when(actorRegistry.createClusterListener()).thenReturn(registryClusterListener);

        clusterClientFactory = new JgroupsClusterClientFactory(ClusterConfigs.jgroupsFactoryConfig(), actorRegistry);

        JChannel jChannel = spy(clusterClientFactory.getJChannel());
        when(jChannel.view()).thenReturn(initialView);
        when(jChannel.getAddress()).thenReturn(address);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(JgroupsClusterClientFactory.class);
        tester.testAllPublicInstanceMethods(clusterClientFactory);
    }

    @Test
    public void constructor_adds_registry_listener() throws Exception {

        verify(actorRegistry).createClusterListener();
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