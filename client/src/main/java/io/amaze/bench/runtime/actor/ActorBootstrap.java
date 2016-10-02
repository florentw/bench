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
import io.amaze.bench.runtime.cluster.ClusterClientFactory;
import io.amaze.bench.runtime.cluster.jms.JMSClusterClientFactory;
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.util.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Acts as entry point for a forked actor JVM, created by a {@link ForkedActorManager}.
 *
 * @see ForkedActorManager
 */
public final class ActorBootstrap {

    private static final Logger log = LogManager.getLogger();

    private final ClusterClientFactory clientFactory;

    ActorBootstrap(@NotNull final ClusterClientFactory clientFactory) {
        this.clientFactory = checkNotNull(clientFactory);
    }

    /**
     * @param args [agentName] [actorName] [className] [jmsServerHost] [jmsServerPort] [temporaryConfigFile]
     * @throws ValidationException if an invalid actor is being created
     * @throws IOException         if an error occurs while reading the actors configuration file
     */
    public static void main(final String[] args) throws ValidationException, IOException {
        checkNotNull(args);

        if (args.length != 5) {
            log.error("Usage:");
            log.error("ActorBootstrap <actorName> <className> " + //
                              "<jmsServerHost> <jmsServerPort> <temporaryConfigFile>");
            throw new IllegalArgumentException();
        }

        ActorKey actorKey = new ActorKey(args[0]);
        String className = args[1];
        String jmsServerHost = args[2];
        int jmsServerPort = Integer.parseInt(args[3]);
        String jsonTmpConfigFile = args[4];

        // Read and delete the temporary config file
        String jsonConfig = Files.readAndDelete(jsonTmpConfigFile);

        JMSEndpoint serverEndpoint = new JMSEndpoint(jmsServerHost, jmsServerPort);
        ClusterClientFactory clientFactory = new JMSClusterClientFactory(serverEndpoint);

        ActorBootstrap actorBootstrap = new ActorBootstrap(clientFactory);
        RuntimeActor actor = actorBootstrap.createActor(actorKey, className, jsonConfig);

        installShutdownHook(actor);
    }

    @VisibleForTesting
    static Thread installShutdownHook(final RuntimeActor actor) {
        ActorShutdownThread hook = new ActorShutdownThread(actor);
        Runtime.getRuntime().addShutdownHook(hook);
        return hook;
    }

    RuntimeActor createActor(final ActorKey key, //
                             final String className, //
                             final String jsonConfig) throws ValidationException, IOException {

        Actors actors = new Actors(clientFactory);
        return actors.create(key, className, jsonConfig);
    }

    static final class ActorShutdownThread extends Thread {

        private final RuntimeActor actor;

        ActorShutdownThread(final RuntimeActor actor) {
            this.actor = checkNotNull(actor);

            setName("actor-shutdown-hook-" + actor);
            setDaemon(true);
        }

        @Override
        public void run() {
            log.info("ShutdownHook called for {}.", actor.getKey());
            actor.close();
        }
    }
}
