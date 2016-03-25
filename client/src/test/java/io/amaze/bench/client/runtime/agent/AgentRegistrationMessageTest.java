package io.amaze.bench.client.runtime.agent;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created on 3/5/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class AgentRegistrationMessageTest {

    @Test
    public void create_msg() {
        AgentRegistrationMessage msg = AgentRegistrationMessage.create();
        assertNotNull(msg);
        assertTrue(msg.getName().startsWith(AgentRegistrationMessage.DEFAULT_AGENT_PREFIX));
        assertNotNull(msg.getSystemInfo());
        assertTrue(msg.getCreationTime() > 0);
    }

}