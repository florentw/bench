/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import io.amaze.bench.api.*;
import io.amaze.bench.api.metric.Metrics;
import io.amaze.bench.cluster.AgentClusterClientFactory;
import io.amaze.bench.cluster.ClusterClient;
import io.amaze.bench.cluster.actor.ActorClusterClient;
import io.amaze.bench.cluster.actor.ActorInputMessage;
import io.amaze.bench.cluster.actor.RuntimeActor;
import io.amaze.bench.cluster.actor.ValidationException;
import io.amaze.bench.cluster.agent.Constants;
import io.amaze.bench.runtime.actor.metric.MetricsInternal;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Optional;

import static io.amaze.bench.shared.util.Reflection.findAtMostOneAnnotatedMethod;
import static java.util.Objects.requireNonNull;


/**
 * Provides instantiation service for actors objects of type {@link RuntimeActor}.<br>
 * Actor's creation follows this process:
 * <ul>
 * <li>Loading the reactor class and checking validity</li>
 * <li>Parsing and loading configuration, checking for validity</li>
 * <li>Creating the actor's {@link ClusterClient}</li>
 * <li>Injecting reactor's dependencies and instantiation</li>
 * <li>Returns a {@link RuntimeActor} object wrapper for the resource manager to use</li>
 * </ul>
 *
 * @see RuntimeActor
 * @see Reactor
 */
public class Actors {

    private final AgentClusterClientFactory clientFactory;

    Actors(@NotNull final AgentClusterClientFactory clientFactory) {
        this.clientFactory = requireNonNull(clientFactory);
    }

    public final RuntimeActor create(@NotNull final ActorKey actorKey,
                                     @NotNull final String className,
                                     @NotNull final String jsonConfig) throws ValidationException {

        // Fail-fast
        Class<? extends Reactor> clazz = ActorValidators.get().loadAndValidate(className);
        Config config = parseConfig(jsonConfig);

        Optional<Method> beforeMethod = findAtMostOneAnnotatedMethod(clazz, Before.class);
        Optional<Method> afterMethod = findAtMostOneAnnotatedMethod(clazz, After.class);
        Optional<Method> bootstrapMethod = findAtMostOneAnnotatedMethod(clazz, Bootstrap.class);

        MetricsInternal metrics = MetricsInternal.create(actorKey);

        ActorClusterClient client = clientFactory.createForActor(actorKey);
        Reactor<Serializable> reactor = createReactor(actorKey, metrics, clazz, client, config);

        return new ActorInternal(actorKey,
                                 metrics,
                                 reactor,
                                 client,
                                 beforeMethod.orElse(null),
                                 afterMethod.orElse(null),
                                 bootstrapMethod.orElse(null));
    }

    private static Config parseConfig(@NotNull final String jsonConfig) throws ValidationException {
        try {
            return ConfigFactory.parseString(jsonConfig, Constants.CONFIG_PARSE_OPTIONS);
        } catch (ConfigException e) {
            throw ValidationException.create("Configuration error", e);
        }
    }

    /**
     * Inject dependencies to create a reactor.
     */
    private Reactor<Serializable> createReactor(final ActorKey key, //
                                                final Metrics metrics, //
                                                final Class<? extends Reactor> clazz, //
                                                final ActorClusterClient client, //
                                                final Config config) {

        MutablePicoContainer pico = new DefaultPicoContainer();

        pico.addComponent(key);

        pico.addComponent(config);

        pico.addComponent((Sender) (to, payload) -> {
            requireNonNull(to);
            requireNonNull(payload);

            client.actorSender().send(new ActorKey(to), ActorInputMessage.message(key.getName(), payload));
        });

        pico.addComponent(metrics);

        pico.addComponent(clazz);

        return pico.getComponent(clazz);
    }
}
