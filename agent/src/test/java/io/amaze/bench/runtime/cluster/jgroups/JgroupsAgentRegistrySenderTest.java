package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.runtime.agent.AgentLifecycleMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created on 10/19/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsAgentRegistrySenderTest {
    @Mock
    private JgroupsSender jgroupsSender;
    private JgroupsAgentRegistrySender sender;

    @Before
    public void init() {
        sender = new JgroupsAgentRegistrySender(jgroupsSender);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(JgroupsAgentRegistrySender.class);
        tester.testAllPublicInstanceMethods(sender);
    }

    @Test
    public void send_to_agent_registry_broadcasts() {
        AgentLifecycleMessage message = AgentLifecycleMessage.closed("key");

        sender.send(message);

        verify(jgroupsSender).broadcast(message);
        verifyNoMoreInteractions(jgroupsSender);
    }

}