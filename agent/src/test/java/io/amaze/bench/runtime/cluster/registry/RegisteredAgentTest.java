package io.amaze.bench.runtime.cluster.registry;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.shared.metric.SystemConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.runtime.agent.AgentTest.DUMMY_AGENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created on 10/14/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class RegisteredAgentTest {

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(SystemConfig.class, SystemConfig.createWithHostname("dummy"));

        tester.testAllPublicConstructors(RegisteredAgent.class);
    }

    @Test
    public void serializable() {
        RegisteredAgent expected = registeredAgent();
        RegisteredAgent actual = SerializableTester.reserialize(expected);

        assertThat(expected.getAgentName(), is(actual.getAgentName()));
        assertThat(expected.getCreationTime(), is(actual.getCreationTime()));
        assertThat(expected.getEndpoint(), is(actual.getEndpoint()));
        assertThat(expected.getSystemConfig(), is(actual.getSystemConfig()));
    }

    @Test
    public void equality() {
        EqualsTester tester = new EqualsTester();
        tester.addEqualityGroup(registeredAgent(), registeredAgent());

        tester.testEquals();
    }

    private RegisteredAgent registeredAgent() {
        return new RegisteredAgent(DUMMY_AGENT,
                                   SystemConfig.createWithHostname("dummy"),
                                   0,
                                   new RegisteredActorTest.DummyEndpoint("endpoint"));
    }

}