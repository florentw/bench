package io.amaze.bench.client.runtime.agent;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.client.runtime.actor.TestActor;
import io.amaze.bench.client.runtime.cluster.ActorCreationRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created on 8/15/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AgentInputMessageTest {

    private AgentInputMessage inputMessage;

    @Before
    public void before() {
        inputMessage = AgentInputMessage.createActor("", new ActorCreationRequest(TestActor.DUMMY_CONFIG));
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(AgentInputMessage.class);
        tester.testAllPublicInstanceMethods(inputMessage);
    }

    @Test
    public void serialize_deserialize() {
        AgentInputMessage received = SerializableTester.reserialize(inputMessage);

        assertThat(received.getAction(), is(inputMessage.getAction()));
        assertThat(received.getCreationRequest(), is(inputMessage.getCreationRequest()));
        assertThat(received.getTargetAgent(), is(inputMessage.getTargetAgent()));
    }

}