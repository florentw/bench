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
package io.amaze.bench.shared.metric;

import com.google.common.testing.NullPointerTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created on 8/15/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class SystemConfigTest {

    private static final MemoryConfig MEMORY_CONFIG = new MemoryConfig(0, new HashMap<>());
    public static final SystemConfig DUMMY_CONFIG = new SystemConfig("",
                                                                     1,
                                                                     "",
                                                                     "",
                                                                     "", MEMORY_CONFIG, new ArrayList<>());

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.setDefault(MemoryConfig.class, MEMORY_CONFIG);
        tester.testAllPublicConstructors(SystemConfig.class);
        tester.testAllPublicInstanceMethods(DUMMY_CONFIG);
    }

}