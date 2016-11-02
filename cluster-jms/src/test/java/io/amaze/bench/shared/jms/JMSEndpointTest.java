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
package io.amaze.bench.shared.jms;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.shared.test.Json;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

/**
 * Created on 9/4/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSEndpointTest {

    @Test(expected = IllegalArgumentException.class)
    public void port_must_be_greater_than_zero() {
        new JMSEndpoint("", -1);
    }

    @Test
    public void toString_yields_valid_json() {
        JMSEndpoint endpoint = createEndpoint();

        assertTrue(Json.isValid(endpoint.toString()));
    }

    @Test
    public void equality() {
        EqualsTester tester = new EqualsTester();
        tester.addEqualityGroup(createEndpoint(), createEndpoint());
        tester.addEqualityGroup(createEndpoint().hashCode(), createEndpoint().hashCode());
        tester.testEquals();
    }

    @Test
    public void serializable() {
        JMSEndpoint expected = createEndpoint();

        JMSEndpoint actual = SerializableTester.reserializeAndAssert(expected);

        assertThat(actual.getHost(), is(expected.getHost()));
        assertThat(actual.getPort(), is(expected.getPort()));
    }

    private JMSEndpoint createEndpoint() {
        return new JMSEndpoint("host", 1337);
    }

}