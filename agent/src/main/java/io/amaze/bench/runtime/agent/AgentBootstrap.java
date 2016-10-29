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

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.cluster.ClusterClientFactory;
import io.amaze.bench.cluster.ClusterClients;
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.actor.ActorManagers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main entry point for agents to boot.
 * <ul>
 * <li>Contains the {@link #main(String[])} method</li>
 * <li>Registers a shutdown hook to be able to close properly the agent upon receiving a termination signal</li>
 * </ul>
 */
public final class AgentBootstrap {

    private static final Logger log = LogManager.getLogger();

    private AgentBootstrap() {
        // Should not be instantiated
    }

    public static void main(final String[] args) {
        if (args.length != 1) {
            log.info("Usage:");
            log.info("$ agent <configFile>");
            return;
        }

        String configFileName = args[0];

        File configFile = new File(configFileName);
        AgentConfig agentConfig = new AgentConfig(configFile);
        ClusterClientFactory clientFactory = ClusterClients.newFactory(ClusterClientFactory.class,
                                                                       agentConfig.clusterConfig(),
                                                                       new ActorRegistry());

        Agent agent = createAgent(clientFactory);
        registerShutdownHook(agent);
    }

    @VisibleForTesting
    static Agent createAgent(final ClusterClientFactory clientFactory) {
        return new Agent(clientFactory, new ActorManagers());
    }

    @VisibleForTesting
    static Thread registerShutdownHook(final Agent agent) {
        AgentShutdownHook hook = new AgentShutdownHook(agent);
        Runtime.getRuntime().addShutdownHook(hook);
        return hook;
    }

    private static final class AgentShutdownHook extends Thread {
        private final Agent agent;

        AgentShutdownHook(final Agent agent) {
            this.agent = checkNotNull(agent);

            setName("agent-shutdown-hook-" + agent);
            setDaemon(true);
        }

        @Override
        public void run() {
            log.info("Calling shutdown hook for agent {}", agent);
            try {
                agent.close();
            } catch (Exception e) {
                log.warn("Error while closing agent {}", agent, e);
            }
        }
    }

}


