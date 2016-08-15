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

import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/20/16.
 */
public final class ProcessorConfig implements Serializable {

    private final String modelName;

    private final int cores;
    private final String frequency;
    private final String cacheSize;

    private final Map<String, String> properties;

    ProcessorConfig(@NotNull final String modelName,
                    @NotNull final int cores,
                    @NotNull final String frequency,
                    @NotNull final String cacheSize,
                    @NotNull final Map<String, String> properties) {

        this.modelName = checkNotNull(modelName);
        this.cores = checkNotNull(cores);
        this.frequency = checkNotNull(frequency);
        this.cacheSize = checkNotNull(cacheSize);
        this.properties = checkNotNull(properties);
    }

    public String getModelName() {
        return modelName;
    }

    public int getCores() {
        return cores;
    }

    public String getCacheSize() {
        return cacheSize;
    }

    public String getFrequency() {
        return frequency;
    }

    public Map<String, String> getProperties() {
        return ImmutableMap.copyOf(properties);
    }

    @Override
    public String toString() {
        return "{\"ProcessorConfig\":{" + //
                "\"modelName\":\"" + modelName + "\"" + ", " + //
                "\"cores\":\"" + cores + "\"" + ", " + //
                "\"frequency\":\"" + frequency + "\"" + ", " + //
                "\"cacheSize\":\"" + cacheSize + "\"" + "}}";
    }
}
