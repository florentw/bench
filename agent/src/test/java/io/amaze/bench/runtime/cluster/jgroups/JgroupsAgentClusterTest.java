package io.amaze.bench.runtime.cluster.jgroups;

import io.amaze.bench.runtime.actor.*;
import io.amaze.bench.runtime.cluster.ActorClusterClient;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.shared.test.IntegrationTest;
import io.amaze.bench.util.ClusterConfigs;
import org.jgroups.JChannel;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created on 10/10/16.
 */
@Category(IntegrationTest.class)
public final class JgroupsAgentClusterTest {

    private static final ActorKey ANOTHER = new ActorKey("another");

    @Test
    public void actor_can_send_message_to_another_one() throws Exception {
        ActorCluster firstActor = createAndInitActor(DUMMY_ACTOR);
        ActorCluster otherActor = createAndInitActor(ANOTHER);

        firstActor.jChannel.send(firstActor.jChannel.getAddress(),
                                 ActorInputMessage.sendMessage("another", TestActor.REPLY_MESSAGE));

        sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

        TestActor testActor1 = (TestActor) firstActor.actorInternal.getInstance();
        testActor1.awaitFirstReceivedMessage();
        TestActor testActor2 = (TestActor) otherActor.actorInternal.getInstance();
        testActor2.awaitFirstReceivedMessage();

        firstActor.actorInternal.close();
        otherActor.actorInternal.close();

        assertThat(testActor1.getReceivedMessages().size(), is(1));
        assertThat(testActor1.getReceivedMessages().get(ANOTHER.getName()).get(0), is(TestActor.REPLY_MESSAGE));
        assertThat(testActor2.getReceivedMessages().size(), is(1));
        assertThat(testActor2.getReceivedMessages().get(DUMMY_ACTOR.getName()).get(0), is(TestActor.REPLY_MESSAGE));
    }

    private ActorCluster createAndInitActor(final ActorKey key) throws Exception {
        ActorRegistry actorRegistry = new ActorRegistry();
        JgroupsClusterClientFactory clientFactory = new JgroupsClusterClientFactory(ClusterConfigs.jgroupsFactoryConfig(), actorRegistry);
        JChannel jChannel = clientFactory.getJChannel();
        ActorClusterClient actorClusterClient = clientFactory.createForActor(key);
        actorClusterClient.sendToActorRegistry(ActorLifecycleMessage.created(key, "agent"));
        ActorInternal actorInternal = (ActorInternal) new Actors(clientFactory).create(key,
                                                                                       TestActor.class.getName(),
                                                                                       "{}");
        actorInternal.init();
        return new ActorCluster(jChannel, actorInternal);
    }

    private static final class ActorCluster {
        private final JChannel jChannel;
        private final ActorInternal actorInternal;

        private ActorCluster(final JChannel jChannel, final ActorInternal actorInternal) {
            this.jChannel = jChannel;
            this.actorInternal = actorInternal;
        }
    }

}
