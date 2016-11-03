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

import io.amaze.bench.shared.util.SystemConfig;
import io.amaze.bench.shared.util.SystemConfigs;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * Created on 3/25/16.
 */
public final class SystemConfigsTest {

    @Test
    public void creates_non_null_system_info() {
        SystemConfig systemConfig = SystemConfigs.get();

        assertNotNull(systemConfig);
        assertFalse(systemConfig.getHostName().isEmpty());
    }

    @Test
    public void second_call_returns_same_instance() {
        SystemConfig first = SystemConfigs.get();
        SystemConfig second = SystemConfigs.get();

        assertSame(first, second);
    }

}