package io.amaze.bench.client.runtime.agent;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import io.amaze.bench.shared.metric.SystemConfig;
import io.amaze.bench.shared.metric.SystemConfigTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created on 3/5/16.
 */
public final class AgentRegistrationMessageTest {

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(SystemConfig.class, SystemConfigTest.DUMMY_CONFIG);
        tester.testAllPublicConstructors(AgentRegistrationMessage.class);
        tester.testAllPublicStaticMethods(AgentRegistrationMessage.class);
    }

    @Test
    public void create_msg() {
        AgentRegistrationMessage msg = AgentRegistrationMessage.create();

        assertNotNull(msg);

        assertTrue(msg.getName().startsWith(AgentRegistrationMessage.DEFAULT_AGENT_PREFIX));
        assertNotNull(msg.getSystemConfig());
        assertTrue(msg.getCreationTime() > 0);
    }

    @Test
    public void equality() {
        AgentRegistrationMessage msg = AgentRegistrationMessage.create();
        AgentRegistrationMessage msgDifferent = AgentRegistrationMessage.create("different");

        new EqualsTester().addEqualityGroup(msg, msg).addEqualityGroup(msg.getName(), msg.getName()).addEqualityGroup(
                msg.hashCode(),
                msg.hashCode()).testEquals();

        assertThat(msg, is(not(msgDifferent)));
    }

    @Test
    public void to_string() {
        AgentRegistrationMessage msg = AgentRegistrationMessage.create();

        assertThat(msg.toString(), is(msg.toString()));
    }

}