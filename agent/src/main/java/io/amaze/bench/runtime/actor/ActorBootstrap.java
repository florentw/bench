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
package io.amaze.bench.runtime.actor;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.amaze.bench.runtime.agent.Constants;
import io.amaze.bench.runtime.cluster.ClusterClientFactory;
import io.amaze.bench.runtime.cluster.ClusterClients;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.shared.util.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.io.Closeable;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Acts as entry point for a forked actor JVM, created by a {@link ForkedActorManager}.
 *
 * @see ForkedActorManager
 */
public class ActorBootstrap implements Closeable {

    private static final Logger log = LogManager.getLogger();

    private final Actors actors;
    private final ClusterClientFactory clientFactory;
    private volatile RuntimeActor actor;

    public ActorBootstrap(@NotNull final ClusterClientFactory clientFactory) throws IOException, ValidationException {
        this.clientFactory = checkNotNull(clientFactory);
        actors = new Actors(this.clientFactory);
    }

    /**
     * @param args [actorName] [className] [temporaryClusterConfigFile] [temporaryActorConfigFile]
     * @throws ValidationException if an invalid actor is being created
     * @throws IOException         if an error occurs while reading the actors configuration file
     */
    public static void main(final String[] args) throws ValidationException, IOException {
        checkNotNull(args);

        if (args.length != 4) {
            log.error("Usage:");
            log.error("ActorBootstrap <actorName> <className> " + //
                              "<tmpClusterConfigFile> <tmpActorConfigFile>");
            throw new IllegalArgumentException();
        }

        ActorKey actorKey = new ActorKey(args[0]);
        String className = args[1];
        String tmpClusterConfig = args[2];
        String tmpActorConfig = args[3];

        log.info("{} starting...", actorKey);

        // Read and delete temporary config files
        Config clusterConfig = parseClusterConfig(Files.readAndDelete(tmpClusterConfig));
        String jsonActorConfig = Files.readAndDelete(tmpActorConfig);

        log.debug("{} bootstrap with clusterConfig {}, actorConfig {}",
                  actorKey,
                  clusterConfig.root().render(ConfigRenderOptions.concise()),
                  jsonActorConfig);

        ClusterClientFactory clientFactory = ClusterClients.newFactory(ClusterClientFactory.class,
                                                                       clusterConfig,
                                                                       new ActorRegistry());

        ActorBootstrap actorBootstrap = new ActorBootstrap(clientFactory);
        RuntimeActor actor = actorBootstrap.createActor(actorKey, className, jsonActorConfig);

        installShutdownHook(actorBootstrap, actor);

        log.info("{} started.", actorKey);
    }

    @VisibleForTesting
    static Thread installShutdownHook(final ActorBootstrap actorBootstrap, final RuntimeActor actor) {
        ActorShutdownThread hook = new ActorShutdownThread(actorBootstrap, actor);
        Runtime.getRuntime().addShutdownHook(hook);
        return hook;
    }

    private static Config parseClusterConfig(@NotNull final String jsonConfig) throws ValidationException {
        try {
            return ConfigFactory.parseString(jsonConfig, Constants.CONFIG_PARSE_OPTIONS);
        } catch (ConfigException e) {
            throw ValidationException.create("Cluster configuration error for " + jsonConfig, e);
        }
    }

    @Override
    public void close() {
        if (actor != null) {
            actor.close();
        }
        clientFactory.close();
    }

    RuntimeActor createActor(final ActorKey key, //
                             final String className, //
                             final String jsonConfig) throws ValidationException, IOException {
        actor = actors.create(key, className, jsonConfig);
        actor.init();
        return actor;
    }

    static final class ActorShutdownThread extends Thread {
        private final ActorBootstrap actorBootstrap;

        ActorShutdownThread(final ActorBootstrap actorBootstrap, final RuntimeActor actor) {
            this.actorBootstrap = checkNotNull(actorBootstrap);

            setName("actor-shutdown-hook-" + actor.getKey());
            setDaemon(true);
        }

        @Override
        public void run() {
            log.info("ShutdownHook called for {}.", actorBootstrap.actor.getKey());
            actorBootstrap.close();
        }
    }
}
