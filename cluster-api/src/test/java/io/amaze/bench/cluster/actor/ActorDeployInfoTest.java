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

import com.google.common.testing.EqualsTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.cluster.registry.RegisteredActorTest;
import io.amaze.bench.shared.test.Json;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created on 9/17/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ActorDeployInfoTest {

    private static final RegisteredActorTest.DummyEndpoint DUMMY_ENDPOINT = new RegisteredActorTest.DummyEndpoint(
            "endpoint");

    @Test
    public void serializable() {
        ActorDeployInfo expected = deployInfo();

        ActorDeployInfo actual = SerializableTester.reserialize(expected);

        assertThat(actual.getPid(), is(expected.getPid()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void pid_should_be_greater_than_zero() {
        new ActorDeployInfo(DUMMY_ENDPOINT, 0);
    }

    @Test
    public void toString_should_yield_valid_json() {
        assertTrue(Json.isValid(deployInfo().toString()));
    }

    @Test
    public void equality() {
        EqualsTester tester = new EqualsTester();
        tester.addEqualityGroup(deployInfo(), deployInfo());
        tester.addEqualityGroup(deployInfo().hashCode(), deployInfo().hashCode());

        tester.testEquals();
    }

    private ActorDeployInfo deployInfo() {
        return new ActorDeployInfo(DUMMY_ENDPOINT, 10);
    }

}