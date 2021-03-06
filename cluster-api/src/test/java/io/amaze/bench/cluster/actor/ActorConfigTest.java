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
package io.amaze.bench.cluster.actor;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.api.ActorKey;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.shared.test.Json;
import org.junit.Test;

import static io.amaze.bench.runtime.actor.TestActor.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created on 4/6/16.
 */
public final class ActorConfigTest {

    @Test
    public void equality() {
        EqualsTester equalsTester = new EqualsTester();
        equalsTester.addEqualityGroup(DUMMY_CONFIG, configForActor(TestActor.class));
        equalsTester.addEqualityGroup(DUMMY_CONFIG.getDeployConfig(),
                                      configForActor(TestActor.class).getDeployConfig());
        equalsTester.addEqualityGroup(DUMMY_CONFIG.hashCode(), configForActor(TestActor.class).hashCode());
        equalsTester.testEquals();
    }

    @Test
    public void null_parameters_for_constructor() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(DeployConfig.class, DUMMY_CONFIG.getDeployConfig());
        tester.setDefault(ActorKey.class, DUMMY_ACTOR);

        tester.testAllPublicConstructors(ActorConfig.class);
        tester.testAllPublicConstructors(DeployConfig.class);
    }

    @Test
    public void serializable() {
        ActorConfig expected = DUMMY_CONFIG;

        ActorConfig actual = SerializableTester.reserializeAndAssert(expected);

        assertThat(actual.getKey(), is(expected.getKey()));
        assertThat(actual.getActorJsonConfig(), is(expected.getActorJsonConfig()));
        assertThat(actual.getClassName(), is(expected.getClassName()));
        assertThat(actual.getDeployConfig(), is(expected.getDeployConfig()));
    }

    @Test
    public void toString_yields_valid_json() {
        assertThat(Json.isValid(DUMMY_CONFIG.toString()), is(true));
    }

}