package io.amaze.bench.runtime.cluster.jgroups;

import io.amaze.bench.api.After;
import io.amaze.bench.runtime.actor.*;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.shared.test.IntegrationTest;
import org.jgroups.JChannel;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created on 10/10/16.
 */
@Category(IntegrationTest.class)
public final class JgroupsAgentClusterTest {

    private JgroupsClusterClientFactory clientFactory;
    private JChannel jChannel;

    @Before
    public void init() throws Exception {
        jChannel = new JChannel("fast.xml");
        clientFactory = new JgroupsClusterClientFactory(jChannel, new ActorRegistry());
        clientFactory.join();

        //new Agent(clientFactory, new ActorManagers());

        assertThat(jChannel.isConnected(), is(true));
    }

    @Ignore
    @Test
    public void actor_can_send_message_to_another_one() throws Exception {
        JChannel jChannel2 = new JChannel("fast.xml");
        JgroupsClusterClientFactory clientFactory2 = new JgroupsClusterClientFactory(jChannel2, new ActorRegistry());
        clientFactory2.join();
        ActorInternal actorInternal = (ActorInternal) new Actors(clientFactory).create(DUMMY_ACTOR,
                                                                                       TestActor.class.getName(),
                                                                                       "{}");
        ActorInternal actorInternal2 = (ActorInternal) new Actors(clientFactory2).create(new ActorKey("another"),
                                                                                         TestActor.class.getName(),
                                                                                         "{}");
        actorInternal.init();
        actorInternal2.init();

        jChannel.send(jChannel.getAddress(), ActorInputMessage.sendMessage("another", TestActor.REPLY_MESSAGE));

        TestActor testActor2 = (TestActor) actorInternal2.getInstance();
        testActor2.awaitFirstReceivedMessage();
        assertThat(testActor2.getReceivedMessages().size(), is(1));
        assertThat(testActor2.getReceivedMessages().get("another").get(0), is("hello"));
    }

    @After
    public void close() {
        clientFactory.join();
        jChannel.close();
    }

}
