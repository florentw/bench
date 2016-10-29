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
package io.amaze.bench.runtime.actor;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.runtime.cluster.actor.DeployConfig;
import io.amaze.bench.shared.test.Json;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created on 9/4/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class DeployConfigTest {

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(DeployConfig.class);
    }

    @Test
    public void equality() {
        EqualsTester tester = new EqualsTester();
        tester.addEqualityGroup(createDeployConfig(), createDeployConfig());
        tester.addEqualityGroup(createDeployConfig().hashCode(), createDeployConfig().hashCode());
        tester.testEquals();
    }

    @Test
    public void toString_yields_valid_json() {
        assertThat(Json.isValid(createDeployConfig().toString()), is(true));
    }

    @Test
    public void serializable() {
        DeployConfig expected = createDeployConfig();

        DeployConfig actual = SerializableTester.reserializeAndAssert(expected);

        assertThat(actual.getPreferredHosts(), is(expected.getPreferredHosts()));
        assertThat(actual.isForked(), is(expected.isForked()));
    }

    private DeployConfig createDeployConfig() {
        return new DeployConfig(true, new ArrayList<>());
    }

}