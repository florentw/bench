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
package io.amaze.bench.runtime.agent;

import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;

/**
 * Created on 3/5/16.
 */
public final class Constants {

    public static final String ACTOR_REGISTRY_TOPIC = "actor-registry";
    public static final String AGENT_REGISTRY_TOPIC = "agent-registry";

    public static final String METRICS_TOPIC = "metrics";

    public static final String AGENTS_TOPIC = "agents";

    public static final String LOG_DIRECTORY_NAME = "logs";

    public static final ConfigParseOptions CONFIG_PARSE_OPTIONS = //
            ConfigParseOptions.defaults() //
                    .setSyntax(ConfigSyntax.JSON) //
                    .setAllowMissing(true);

    private Constants() {
        // Should not be instantiated
    }

}
