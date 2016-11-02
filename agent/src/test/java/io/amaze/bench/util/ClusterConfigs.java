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
package io.amaze.bench.util;

import com.google.common.base.Throwables;

/**
 * Created on 10/13/16.
 */
public final class ClusterConfigs {

    public static final String JMS_CLUSTER_CONFIGS = "io.amaze.bench.shared.jms.JMSClusterConfigs";
    public static final String JGROUPS_CLUSTER_CONFIGS = "io.amaze.bench.shared.jgroups.JgroupsClusterConfigs";

    public static TestClusterConfigs defaultConfig() {
        return jms();
    }

    public static TestClusterConfigs jms() {
        return loadConfig(JMS_CLUSTER_CONFIGS);
    }

    public static TestClusterConfigs jgroups() {
        return loadConfig(JGROUPS_CLUSTER_CONFIGS);
    }

    private static TestClusterConfigs loadConfig(final String configsClassName) {
        try {
            return Class.forName(configsClassName).asSubclass(TestClusterConfigs.class).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw Throwables.propagate(e);
        }
    }
}
