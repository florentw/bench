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
package io.amaze.bench.cluster.registry;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.Endpoint;
import io.amaze.bench.cluster.actor.ActorDeployInfo;
import io.amaze.bench.cluster.actor.ActorKey;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.shared.test.Json;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.Objects;

import static io.amaze.bench.cluster.agent.AgentUtil.DUMMY_AGENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

/**
 * Created on 10/14/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class RegisteredActorTest {

    private static final ActorKey ACTOR_KEY = new ActorKey("dummy");
    @Mock
    private Endpoint endpoint;

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorKey.class, ACTOR_KEY);
        tester.setDefault(ActorDeployInfo.class, new ActorDeployInfo(endpoint, 10));
        tester.setDefault(RegisteredActor.class, registeredActor(ACTOR_KEY));
        tester.setDefault(AgentKey.class, DUMMY_AGENT);

        tester.testAllPublicConstructors(RegisteredActor.class);
        tester.testAllPublicStaticMethods(RegisteredActor.class);
        tester.testAllPublicInstanceMethods(registeredActor(ACTOR_KEY));
    }

    @Test
    public void serializable() {
        RegisteredActor expected = registeredActor(ACTOR_KEY);
        RegisteredActor actual = SerializableTester.reserialize(expected);

        assertThat(expected.getAgent(), is(actual.getAgent()));
        assertThat(expected.getDeployInfo(), is(actual.getDeployInfo()));
        assertThat(expected.getKey(), is(actual.getKey()));
        assertThat(expected.getState(), is(actual.getState()));
    }

    @Test
    public void toString_yields_valid_json() {
        assertTrue(Json.isValid(registeredActor(ACTOR_KEY).toString()));
    }

    private RegisteredActor registeredActor(final ActorKey actorKey) {
        return RegisteredActor.created(actorKey, DUMMY_AGENT);
    }

    public static class DummyEndpoint implements Endpoint, Serializable {

        private final String key;

        public DummyEndpoint(final String key) {
            this.key = key;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DummyEndpoint that = (DummyEndpoint) o;
            return Objects.equals(key, that.key);
        }
    }

}