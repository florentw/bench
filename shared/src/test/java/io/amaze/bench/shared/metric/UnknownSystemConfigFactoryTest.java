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

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created on 3/20/16.
 */
public final class UnknownSystemConfigFactoryTest {

    @Test
    public void call_to_create_populates_system_info() {
        UnknownSystemConfigFactory infoFactory = new UnknownSystemConfigFactory();
        SystemConfig sysConfig = infoFactory.create();

        assertFalse(sysConfig.getHostName().isEmpty());
        assertFalse(sysConfig.getOsName().isEmpty());
        assertFalse(sysConfig.getOsVersion().isEmpty());
        assertFalse(sysConfig.getProcArch().isEmpty());
        assertTrue(sysConfig.getProcCount() > 0);
        assertTrue(sysConfig.getProcessors().isEmpty());
    }

}