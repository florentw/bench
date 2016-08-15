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

import java.util.Collections;

/**
 * Created on 3/20/16.
 */
final class UnknownSystemConfigFactory extends AbstractSystemConfigFactory {

    @Override
    public SystemConfig create() {
        return new SystemConfig(getHostName(),
                                getNbProcs(),
                                getOsArch(),
                                getOsName(),
                                getOsVersion(),
                                getMemoryInfo(),
                                Collections.<ProcessorConfig>emptyList());
    }

    private MemoryConfig getMemoryInfo() {
        return new MemoryConfig(UNKNOWN_INT_VALUE, Collections.<String, String>emptyMap());
    }

}
