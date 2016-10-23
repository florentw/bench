package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.runtime.actor.ActorDeployInfo;
import io.amaze.bench.runtime.actor.ActorInputMessage;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.RegisteredActor;
import io.amaze.bench.shared.jgroups.JgroupsEndpoint;
import org.jgroups.Address;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.NoSuchElementException;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.runtime.cluster.registry.RegisteredActor.created;
import static io.amaze.bench.runtime.cluster.registry.RegisteredActor.initialized;
import static org.mockito.Mockito.*;

/**
 * Created on 10/19/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsActorSenderTest {

    @Mock
    private JgroupsSender jgroupsSender;
    @Mock
    private ActorRegistry actorRegistry;
    @Mock
    private Address address;

    private JgroupsEndpoint endpoint;

    private JgroupsActorSender sender;

    @Before
    public void init() {
        sender = new JgroupsActorSender(jgroupsSender, actorRegistry);
        endpoint = new JgroupsEndpoint(address);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(JgroupsSender.class, jgroupsSender);
        tester.setDefault(ActorInputMessage.class, ActorInputMessage.dumpMetrics());
        tester.setDefault(ActorKey.class, new ActorKey("dummy"));

        tester.testAllPublicConstructors(JgroupsActorSender.class);
        tester.testAllPublicInstanceMethods(sender);
    }

    @Test
    public void send_to_actor_resolves_endpoint_through_registry_and_sends() throws Exception {
        RegisteredActor agent = created(DUMMY_ACTOR, "agent");
        when(actorRegistry.byKey(DUMMY_ACTOR)).thenReturn(initialized(agent, new ActorDeployInfo(endpoint, 10)));
        ActorInputMessage message = ActorInputMessage.sendMessage("other", "hello");

        sender.send(DUMMY_ACTOR, message);

        verify(jgroupsSender).sendToEndpoint(endpoint, message);
        verifyNoMoreInteractions(jgroupsSender);
    }

    @Test(expected = NoSuchElementException.class)
    public void sending_to_unknown_actor_throws_NoSuchElementException() {
        sender.send(DUMMY_ACTOR, ActorInputMessage.sendMessage("other", "hello"));
    }

    @Test(expected = NoSuchElementException.class)
    public void sending_to_uninitialized_actor_throws_NoSuchElementException() {
        when(actorRegistry.byKey(DUMMY_ACTOR)).thenReturn(created(DUMMY_ACTOR, "agent"));

        sender.send(DUMMY_ACTOR, ActorInputMessage.sendMessage("other", "hello"));
    }

}