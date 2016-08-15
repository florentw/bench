package io.amaze.bench.client.runtime.agent;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
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
public final class AgentOutputMessageTest {

    private AgentOutputMessage outputMessage;

    @Before
    public void before() {
        outputMessage = new AgentOutputMessage(AgentOutputMessage.Action.ACTOR_LIFECYCLE, "");
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(AgentOutputMessage.class);
        tester.testAllPublicInstanceMethods(outputMessage);
    }

    @Test
    public void serialize_deserialize() {
        AgentOutputMessage received = SerializableTester.reserialize(outputMessage);

        assertThat(received.getAction(), is(outputMessage.getAction()));
        assertThat(received.getData(), is(outputMessage.getData()));
    }

}