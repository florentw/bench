package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created on 10/14/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AgentViewTest {

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(AgentView.class);
        tester.testAllPublicInstanceMethods(agentView());
    }

    @Test
    public void serializable() {
        AgentView expected = agentView();
        AgentView actual = SerializableTester.reserialize(expected);

        assertThat(expected.getRegisteredAgents(), is(actual.getRegisteredAgents()));
    }

    private AgentView agentView() {
        return new AgentView(new HashSet<>());
    }
}