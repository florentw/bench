package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.RegisteredActor;
import io.amaze.bench.shared.jgroups.JgroupsEndpoint;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.Util;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.UnknownHostException;
import java.util.NoSuchElementException;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 10/9/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsSenderTest {

    @Mock
    private JChannel jChannel;
    @Mock
    private ActorRegistry actorRegistry;

    private JgroupsSender sender;
    private Endpoint endpoint;
    private Address address;

    @Before
    public void init() throws UnknownHostException {
        sender = new JgroupsSender(jChannel, actorRegistry);
        address = new IpAddress("localhost", 1337);
        endpoint = new JgroupsEndpoint(address);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorKey.class, new ActorKey("dummy"));

        tester.testAllPublicConstructors(JgroupsSender.class);
        tester.testAllPublicInstanceMethods(sender);
    }

    @Test
    public void broadcast_send_message_with_null_recipient() throws Exception {
        sender.broadcast("hello");

        verify(jChannel).send(eq(null), aryEq(Util.objectToByteBuffer("hello")));
        verifyNoMoreInteractions(jChannel);
    }

    @Test
    public void send_to_actor_resolves_endpoint_through_registry_and_sends() throws Exception {
        when(actorRegistry.byKey(DUMMY_ACTOR)).thenReturn(RegisteredActor.created(DUMMY_ACTOR, "agent", endpoint));

        sender.sendToActor(DUMMY_ACTOR, "hello");

        verify(jChannel).send(eq(address), aryEq(Util.objectToByteBuffer("hello")));
        verifyNoMoreInteractions(jChannel);
    }

    @Test(expected = NoSuchElementException.class)
    public void sending_to_unknown_actor_throws_NoSuchElementException() {
        sender.sendToActor(DUMMY_ACTOR, "hello");
    }

}