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
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/20/16.
 */
public final class MemoryConfig implements Serializable {

    private final long totalMemoryKb;
    private final Map<String, String> memoryProperties;

    public MemoryConfig(final long totalMemoryKb, @NotNull final Map<String, String> memoryProperties) {
        this.totalMemoryKb = totalMemoryKb;
        this.memoryProperties = checkNotNull(memoryProperties);
    }

    public long getTotalMemoryKb() {
        return totalMemoryKb;
    }

    public Map<String, String> getMemoryProperties() {
        return memoryProperties;
    }

    @Override
    public String toString() {
        return "{\"MemoryConfig\":{" + //
                "\"totalMemoryKb\":\"" + totalMemoryKb + "\"" + ", " + //
                "\"memoryProperties\":\"" + memoryProperties + "\"}}";
    }
}
