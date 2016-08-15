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

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/20/16.
 */
public final class SystemConfig implements Serializable {

    private final String hostName;
    private final int procCount;
    private final String procArch;
    private final String osName;
    private final String osVersion;

    private final List<ProcessorConfig> processors;
    private final MemoryConfig memoryConfig;

    public SystemConfig(@NotNull final String hostName,
                        @NotNull final int procCount,
                        @NotNull final String procArch,
                        @NotNull final String osName,
                        @NotNull final String osVersion,
                        @NotNull final MemoryConfig memoryConfig,
                        @NotNull final List<ProcessorConfig> processors) {

        this.hostName = checkNotNull(hostName);
        this.procCount = procCount;
        this.procArch = checkNotNull(procArch);
        this.osName = checkNotNull(osName);
        this.osVersion = checkNotNull(osVersion);
        this.memoryConfig = checkNotNull(memoryConfig);
        this.processors = checkNotNull(processors);
    }

    public String getHostName() {
        return hostName;
    }

    public int getProcCount() {
        return procCount;
    }

    public String getProcArch() {
        return procArch;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public MemoryConfig getMemoryConfig() {
        return memoryConfig;
    }

    public List<ProcessorConfig> getProcessors() {
        return processors;
    }

    @Override
    public String toString() {
        return "{\"SystemConfig\":{" + //
                "\"hostName\":\"" + hostName + "\"" + ", " + //
                "\"procCount\":\"" + procCount + "\"" + ", " + //
                "\"procArch\":\"" + procArch + "\"" + ", " + //
                "\"osName\":\"" + osName + "\"" + ", " + //
                "\"osVersion\":\"" + osVersion + "\"" + ", " + //
                "\"processors\":" + Arrays.toString(processors.toArray(new ProcessorConfig[0])) + ", " + //
                "\"memoryConfig\":" + memoryConfig + "}}";
    }
}
