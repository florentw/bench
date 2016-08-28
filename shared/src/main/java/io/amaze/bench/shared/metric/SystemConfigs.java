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


import oshi.json.SystemInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Provides an instance of {@link SystemConfig}, populated with the configuration of the current box.
 */
public final class SystemConfigs {

    static final String UNKNOWN_STRING_VALUE = "[Unknown]";
    private static final SystemConfig SYSTEM_CONFIG = create();

    private SystemConfigs() {
        // Should not be instantiated
    }

    public static SystemConfig get() {
        return SYSTEM_CONFIG;
    }

    private static SystemConfig create() {
        SystemInfo systemInfo = new SystemInfo();
        return new SystemConfig(getHostName(),
                                systemInfo.getOperatingSystem().toCompactJSON(),
                                systemInfo.getHardware().getProcessor().toCompactJSON(),
                                systemInfo.getHardware().getMemory().toCompactJSON());
    }

    private static String getHostName() {
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignore) { // NOSONAR
            hostName = UNKNOWN_STRING_VALUE;
        }
        return hostName;
    }

}
