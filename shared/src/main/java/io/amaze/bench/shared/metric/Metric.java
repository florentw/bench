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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 2/24/16.
 */
public final class Metric implements Serializable {
    private final String label;
    private final String firstUnit;
    private final String secondUnit;
    private final Number value;

    public Metric(@NotNull final String label, @NotNull final String firstUnit, @NotNull final Number value) {
        this(label, firstUnit, null, value);
    }

    public Metric(@NotNull final String label, @NotNull final String firstUnit, final String secondUnit,
                  @NotNull final Number value) {

        this.label = checkNotNull(label);
        this.firstUnit = checkNotNull(firstUnit);
        this.value = checkNotNull(value);
        this.secondUnit = secondUnit;
    }

    public String getLabel() {
        return label;
    }

    public Number getValue() {
        return value;
    }

    public String getSecondUnit() {
        return secondUnit;
    }

    public String getFirstUnit() {
        return firstUnit;
    }
}
