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
package io.amaze.bench.shared.util;

import com.google.common.annotations.VisibleForTesting;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/20/16.
 */
public final class SystemConfig implements Serializable {

    private final String hostName;
    private final String operatingSystemJson;
    private final String fileSystemJson;
    private final String processorJson;
    private final String memoryJson;

    SystemConfig(@NotNull final String hostName,
                 @NotNull final String operatingSystemJson,
                 @NotNull final String fileSystemJson,
                 @NotNull final String processorJson,
                 @NotNull final String memoryJson) {

        this.hostName = checkNotNull(hostName);
        this.operatingSystemJson = checkNotNull(operatingSystemJson);
        this.fileSystemJson = checkNotNull(fileSystemJson);
        this.processorJson = checkNotNull(processorJson);
        this.memoryJson = checkNotNull(memoryJson);
    }

    @VisibleForTesting
    public static SystemConfig createWithHostname(String hostName) {
        return new SystemConfig(hostName, "", "", "", "");
    }

    public String getHostName() {
        return hostName;
    }

    public String getOperatingSystemJson() {
        return operatingSystemJson;
    }

    public String getFileSystemJson() {
        return fileSystemJson;
    }

    public String getProcessorJson() {
        return processorJson;
    }

    public String getMemoryJson() {
        return memoryJson;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostName, operatingSystemJson, fileSystemJson, processorJson, memoryJson);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SystemConfig that = (SystemConfig) o;
        return Objects.equals(hostName, that.hostName) && //
                Objects.equals(operatingSystemJson, that.operatingSystemJson) && //
                Objects.equals(fileSystemJson, that.fileSystemJson) && //
                Objects.equals(processorJson, that.processorJson) && //
                Objects.equals(memoryJson, that.memoryJson);
    }

    @Override
    @NotNull
    public String toString() {
        return "{\"SystemConfig\":{" + //
                "\"hostName\":\"" + hostName + "\"" + ", " + //
                "\"operatingSystem\":" + operatingSystemJson + ", " + //
                "\"fileSystem\":" + fileSystemJson + ", " + //
                "\"processor\":" + processorJson + ", " + //
                "\"memory\":" + memoryJson + "}}";
    }
}
