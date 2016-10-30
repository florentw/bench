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
package io.amaze.bench.cluster.actor;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.leader.registry.RegisteredActorTest;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.shared.test.Json;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.cluster.agent.AgentUtil.DUMMY_AGENT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created on 8/15/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ActorLifecycleMessageTest {

    private static final RegisteredActorTest.DummyEndpoint DUMMY_ENDPOINT = new RegisteredActorTest.DummyEndpoint(
            "endpoint");
    private static final ActorDeployInfo ACTOR_DEPLOY_INFO = new ActorDeployInfo(DUMMY_ENDPOINT, 10);

    private ActorLifecycleMessage msg;

    @Before
    public void init() {
        msg = ActorLifecycleMessage.initialized(TestActor.DUMMY_ACTOR, ACTOR_DEPLOY_INFO);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorDeployInfo.class, ACTOR_DEPLOY_INFO);
        tester.setDefault(ActorKey.class, TestActor.DUMMY_ACTOR);
        tester.setDefault(AgentKey.class, DUMMY_AGENT);

        tester.testAllPublicStaticMethods(ActorLifecycleMessage.class);
        tester.testAllPublicInstanceMethods(msg);
    }

    @Test
    public void serialize_deserialize() {
        ActorLifecycleMessage received = SerializableTester.reserialize(msg);

        assertThat(received.getActor(), is(msg.getActor()));
        assertThat(received.getAgent(), is(msg.getAgent()));
        assertThat(received.getState(), is(msg.getState()));
        assertThat(received.getDeployInfo(), is(msg.getDeployInfo()));
        assertThat(received.getThrowable(), is(msg.getThrowable()));
    }

    @Test
    public void toString_should_yield_valid_json() {
        assertTrue(Json.isValid(msg.toString()));
        assertTrue(Json.isValid(ActorLifecycleMessage.created(TestActor.DUMMY_ACTOR, DUMMY_AGENT).toString()));
    }

}