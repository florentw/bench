/*
 * Copyright 2016 Florent Weber <florent.weber@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.amaze.bench.runtime.cluster.jgroups;

import io.amaze.bench.cluster.actor.ActorClusterClient;
import io.amaze.bench.cluster.actor.ActorInputMessage;
import io.amaze.bench.cluster.actor.ActorKey;
import io.amaze.bench.cluster.actor.ActorLifecycleMessage;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.leader.registry.ActorRegistry;
import io.amaze.bench.runtime.actor.ActorInternal;
import io.amaze.bench.runtime.actor.Actors;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.shared.test.IntegrationTest;
import io.amaze.bench.util.ClusterConfigs;
import org.jgroups.JChannel;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.runtime.actor.TestActor.REPLY_MESSAGE;
import static junit.framework.TestCase.assertTrue;
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

        sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

        firstActor.jChannel.send(firstActor.jChannel.getAddress(),
                                 new JgroupsActorMessage(DUMMY_ACTOR,
                                                         ActorInputMessage.sendMessage("another", REPLY_MESSAGE)));

        sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

        TestActor testActor1 = (TestActor) firstActor.actorInternal.getInstance();
        assertTrue(testActor1.awaitFirstReceivedMessage());
        TestActor testActor2 = (TestActor) otherActor.actorInternal.getInstance();
        assertTrue(testActor2.awaitFirstReceivedMessage());

        firstActor.actorInternal.close();
        otherActor.actorInternal.close();

        assertThat(testActor1.getReceivedMessages().size(), is(1));
        assertThat(testActor1.getReceivedMessages().get(ANOTHER.getName()).get(0), is(REPLY_MESSAGE));
        assertThat(testActor2.getReceivedMessages().size(), is(1));
        assertThat(testActor2.getReceivedMessages().get(DUMMY_ACTOR.getName()).get(0), is(REPLY_MESSAGE));
    }

    private ActorCluster createAndInitActor(final ActorKey key) throws Exception {
        ActorRegistry actorRegistry = new ActorRegistry();
        JgroupsClusterClientFactory clientFactory = new JgroupsClusterClientFactory(ClusterConfigs.jgroupsFactoryConfig(),
                                                                                    actorRegistry);
        JChannel jChannel = clientFactory.getJChannel();
        ActorClusterClient actorClusterClient = clientFactory.createForActor(key);
        actorClusterClient.actorRegistrySender().send(ActorLifecycleMessage.created(key, new AgentKey("agent")));
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
