/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
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
package io.amaze.bench.actor;

import com.google.common.base.Throwables;
import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.actor.ActorConfig;
import io.amaze.bench.cluster.actor.DeployConfig;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.leader.Actors;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.runtime.agent.Agent;
import io.amaze.bench.shared.test.IntegrationTest;
import io.amaze.bench.util.BenchRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;
import static io.amaze.bench.runtime.actor.TestActor.REPLY_MESSAGE;
import static io.amaze.bench.runtime.actor.TestActor.runningActors;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created on 11/4/16.
 */
@Category(IntegrationTest.class)
@RunWith(Theories.class)
public final class ActorsMessagingTest {

    @DataPoints
    public static final BenchRule[] benchRules = new BenchRule[]{ //
            BenchRule.newJmsCluster(), //
            BenchRule.newJgroupsCluster() //
    };
    private static final ActorKey ACTOR_1 = new ActorKey("actor-1");
    private static final ActorKey ACTOR_2 = new ActorKey("actor-2");

    @Rule
    public final Timeout globalTimeout = new Timeout(30, TimeUnit.SECONDS);
    private BenchRule benchRule;
    private Agent agent;

    @Theory
    public void two_actors_sends_message_to_each_other(final BenchRule benchRule) throws ExecutionException {
        before(benchRule);

        Actors.ActorHandle actor1 = createActor(benchRule, ACTOR_1);
        Actors.ActorHandle actor2 = createActor(benchRule, ACTOR_2);

        actor1.send("test", REPLY_MESSAGE + ":" + ACTOR_2.getName());
        actor2.send("test", REPLY_MESSAGE + ":" + ACTOR_1.getName());

        Map<String, List<String>> msgs1 = runningActors().get(ACTOR_1).awaitFirstAndReturnMessages();
        Map<String, List<String>> msgs2 = runningActors().get(ACTOR_2).awaitFirstAndReturnMessages();

        assertThat(msgs1.size(), is(1));
        assertThat(msgs2.size(), is(1));
        assertThat(msgs1.get(ACTOR_2.getName()).get(0), is("hello"));
        assertThat(msgs2.get(ACTOR_1.getName()).get(0), is("hello"));

        actor1.close();
        actor2.close();
    }

    @After
    public void after() throws Exception {
        try {
            agent.close();
        } finally {
            benchRule.after();
        }
    }

    private Actors.ActorHandle createActor(final BenchRule benchRule, final ActorKey actor) throws ExecutionException {
        Actors.ActorHandle actorHandle = benchRule.actors().create(createConfig(actor));
        getUninterruptibly(actorHandle.actorInitialization());
        return actorHandle;
    }

    private ActorConfig createConfig(final ActorKey key) {
        return new ActorConfig(key, TestActor.class.getName(), new DeployConfig(false, new ArrayList<>()), "{}");
    }

    private void before(final BenchRule benchRule) {
        this.benchRule = benchRule;
        benchRule.before();
        try {
            agent = getUninterruptibly(this.benchRule.agents().create(new AgentKey("test-agent")));
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }
}
