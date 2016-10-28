package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import io.amaze.bench.runtime.cluster.ActorClusterClient;
import io.amaze.bench.runtime.cluster.AgentClusterClient;
import io.amaze.bench.runtime.cluster.registry.*;
import io.amaze.bench.shared.jgroups.JgroupsClusterMember;
import io.amaze.bench.shared.util.Network;
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
    private AgentRegistry agentRegistry;
    @Mock
    private Address address;
    @Mock
    private ActorRegistryListener registryClusterListener;
    @Mock
    private JChannel jChannel;
    @Mock
    private Config config;

    private JgroupsClusterClientFactory clusterClientFactory;

    @Before
    public void init() throws UnknownHostException {
        View initialView = new View(new ViewId(), new Address[]{new IpAddress(Network.LOCALHOST, 1337)});
        when(jChannel.view()).thenReturn(initialView);
        when(jChannel.getAddress()).thenReturn(address);
        when(actorRegistry.createClusterListener()).thenReturn(registryClusterListener);

        clusterClientFactory = new JgroupsClusterClientFactory(jChannel, actorRegistry, config);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicInstanceMethods(clusterClientFactory);
    }

    @Test
    public void constructor_with_config_parameter_creates_jChannel() {
        clusterClientFactory = new JgroupsClusterClientFactory(ClusterConfigs.jgroupsFactoryConfig(), actorRegistry);

        assertNotNull(clusterClientFactory.getJChannel());
    }

    @Test(expected = ConfigException.class)
    public void constructor_with_config_parameter_invalid_config_throws() {
        clusterClientFactory = new JgroupsClusterClientFactory(ClusterConfigs.invalidClassClusterConfig(),
                                                               actorRegistry);
    }

    @Test
    public void constructor_adds_registry_listener_and_joins_cluster() throws Exception {

        verify(actorRegistry).createClusterListener();
        verify(jChannel).receiver(any(JgroupsClusterMember.class));
        verify(jChannel).connect(anyString());
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

    @Test
    public void create_for_agent_registry_returns_instance() {
        when(agentRegistry.createClusterListener()).thenReturn(mock(AgentRegistryListener.class));

        AgentRegistryClusterClient clusterClient = clusterClientFactory.createForAgentRegistry(agentRegistry);

        assertNotNull(clusterClient);
        verify(agentRegistry).createClusterListener();
        verifyNoMoreInteractions(agentRegistry);
    }

    @Test
    public void clusterConfigFactory_returns_instance() {
        assertNotNull(clusterClientFactory.clusterConfigFactory());
    }

    @Test
    public void close_closes_jChannel() {

        clusterClientFactory.close();

        verify(jChannel).close();
    }
}