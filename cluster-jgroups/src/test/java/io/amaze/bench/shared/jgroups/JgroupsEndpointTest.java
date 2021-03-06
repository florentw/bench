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
package io.amaze.bench.shared.jgroups;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.shared.test.Json;
import io.amaze.bench.shared.util.Network;
import org.jgroups.stack.IpAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * Created on 10/9/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsEndpointTest {

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(JgroupsEndpoint.class);
    }

    @Test
    public void equality() throws Exception {
        EqualsTester tester = new EqualsTester();
        tester.addEqualityGroup(jgroupsEndpoint(), jgroupsEndpoint());
        tester.addEqualityGroup(jgroupsEndpoint().hashCode(), jgroupsEndpoint().hashCode());

        tester.testEquals();
    }

    @Test
    public void serializable() throws Exception {
        JgroupsEndpoint expected = jgroupsEndpoint();

        JgroupsEndpoint actual = SerializableTester.reserialize(expected);

        assertThat(expected.getAddress(), is(actual.getAddress()));
        assertThat(expected, is(actual));
    }

    @Test
    public void toString_yields_valid_json() throws Exception {
        assertTrue(Json.isValid(jgroupsEndpoint().toString()));
    }

    private JgroupsEndpoint jgroupsEndpoint() throws Exception {
        IpAddress address = new IpAddress(Network.LOCALHOST, 1337);
        return new JgroupsEndpoint(address);
    }

}