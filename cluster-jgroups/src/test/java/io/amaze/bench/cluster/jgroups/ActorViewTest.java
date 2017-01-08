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
package io.amaze.bench.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created on 10/2/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ActorViewTest {

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(ActorView.class);
        tester.testAllPublicInstanceMethods(actorView());
    }

    @Test
    public void serializable() {
        ActorView expected = actorView();
        ActorView actual = SerializableTester.reserialize(expected);

        assertThat(expected.registeredActors(), is(actual.registeredActors()));
    }

    private ActorView actorView() {
        return new ActorView(new HashSet<>());
    }

}