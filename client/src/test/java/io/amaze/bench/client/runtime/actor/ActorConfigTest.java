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
package io.amaze.bench.client.runtime.actor;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import org.junit.Test;

import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_CONFIG;
import static io.amaze.bench.client.runtime.actor.TestActor.configForActor;

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
        NullPointerTester test = new NullPointerTester();
        test.setDefault(DeployConfig.class, DUMMY_CONFIG.getDeployConfig());
        test.testAllPublicConstructors(ActorConfig.class);
        test.testAllPublicConstructors(DeployConfig.class);
    }

}