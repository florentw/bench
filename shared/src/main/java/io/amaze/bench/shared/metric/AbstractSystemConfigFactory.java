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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created on 3/20/16.
 */
abstract class AbstractSystemConfigFactory implements SystemConfigFactory {

    static final String UNKNOWN_STRING_VALUE = "[Unknown]";
    static final int UNKNOWN_INT_VALUE = -1;

    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    final int getNbProcs() {
        return osBean.getAvailableProcessors();
    }

    final String getOsArch() {
        return osBean.getArch();
    }

    final String getOsName() {
        return osBean.getName();
    }

    final String getOsVersion() {
        return osBean.getVersion();
    }

    final String getHostName() {
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignore) { // NOSONAR
            hostName = UNKNOWN_STRING_VALUE;
        }
        return hostName;
    }


}
