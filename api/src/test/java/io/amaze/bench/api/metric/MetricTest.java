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
package io.amaze.bench.api.metric;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created on 9/6/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class MetricTest {

    private static final Metric DUMMY = Metric.metric("speed", "km/s") //
            .label("label") //
            .minValue(0) //
            .maxValue(10) //
            .build();

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(Metric.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void empty_key_is_invalid() {
        Metric.metric("", "m").build();
    }

    @Test
    public void serializable() {

        Metric actual = SerializableTester.reserialize(DUMMY);

        assertThat(actual.getKey(), is(DUMMY.getKey()));
        assertThat(actual.getLabel(), is(actual.getLabel()));
        assertThat(actual.getUnit(), is(actual.getUnit()));
        assertThat(actual.getMinValue(), is(actual.getMinValue()));
        assertThat(actual.getMaxValue(), is(actual.getMaxValue()));
    }

    @Test
    public void equality() {
        EqualsTester tester = new EqualsTester();
        tester.addEqualityGroup(metric(), createMetric());
        tester.addEqualityGroup(metric().hashCode(), metric().hashCode());
        tester.testEquals();
    }

    private Metric metric() {
        return Metric.metric("test", "unit").build();
    }

    private Metric createMetric() {
        return metric();
    }

}