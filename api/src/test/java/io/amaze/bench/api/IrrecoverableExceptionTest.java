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
package io.amaze.bench.api;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created on 8/15/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class IrrecoverableExceptionTest {

    private IrrecoverableException exception;

    @org.junit.Before
    public void before() {
        exception = new IrrecoverableException("", new RuntimeException());
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(IrrecoverableException.class);
        tester.testAllPublicInstanceMethods(exception);
    }

    @Test
    public void serialize_deserialize() {
        IrrecoverableException received = SerializableTester.reserialize(exception);

        assertThat(received.getMessage(), is(exception.getMessage()));
        assertTrue(exception.getCause() instanceof RuntimeException);
    }

}