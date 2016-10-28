package io.amaze.bench.runtime.cluster.jgroups;

import io.amaze.bench.api.After;
import io.amaze.bench.runtime.actor.ActorInputMessage;
import io.amaze.bench.runtime.actor.ActorInternal;
import io.amaze.bench.runtime.actor.Actors;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.shared.test.IntegrationTest;
import io.amaze.bench.util.ClusterConfigs;
import org.jgroups.JChannel;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created on 10/9/16.
 */
@Category(IntegrationTest.class)
public final class JgroupsActorClusterTest {

    private JgroupsClusterClientFactory clientFactory;
    private JChannel jChannel;

    @Before
    public void init() throws Exception {
        clientFactory = new JgroupsClusterClientFactory(ClusterConfigs.jgroupsFactoryConfig(), new ActorRegistry());
        jChannel = clientFactory.getJChannel();

        assertThat(jChannel.isConnected(), is(true));
    }

    @Test
    public void created_actor_can_receive_a_message() throws Exception {
        Actors actors = new Actors(clientFactory);
        ActorInternal actorInternal = (ActorInternal) actors.create(DUMMY_ACTOR, TestActor.class.getName(), "{}");
        actorInternal.init();

        jChannel.send(jChannel.getAddress(),
                      new JgroupsActorMessage(DUMMY_ACTOR, ActorInputMessage.sendMessage("test", "hello")));

        TestActor testActor = (TestActor) actorInternal.getInstance();
        assertTrue(testActor.awaitFirstReceivedMessage());
        assertThat(testActor.getReceivedMessages().size(), is(1));
        assertThat(testActor.getReceivedMessages().get("test").get(0), is("hello"));
    }

    @After
    public void close() {
        jChannel.close();
    }
}
