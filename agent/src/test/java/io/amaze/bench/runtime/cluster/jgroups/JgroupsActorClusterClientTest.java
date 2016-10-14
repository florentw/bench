package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.runtime.message.Message;
import io.amaze.bench.shared.jgroups.JgroupsListenerMultiplexer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created on 10/14/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsActorClusterClientTest {

    @Mock
    JgroupsListenerMultiplexer listenerMultiplexer;
    @Mock
    JgroupsSender jgroupsSender;

    private JgroupsActorClusterClient clusterClient;

    @Before
    public void init() {
        clusterClient = new JgroupsActorClusterClient(listenerMultiplexer, jgroupsSender);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(Message.class, new Message("", ""));

        tester.testAllPublicConstructors(JgroupsActorClusterClient.class);
        tester.testAllPublicInstanceMethods(clusterClient);
    }

}