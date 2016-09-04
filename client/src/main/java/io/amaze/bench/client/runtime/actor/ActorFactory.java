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
package io.amaze.bench.client.runtime.actor;

import com.typesafe.config.*;
import io.amaze.bench.api.*;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorActor;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;
import io.amaze.bench.shared.metric.MetricsSink;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.amaze.bench.shared.helper.Reflection.findAtMostOneAnnotatedMethod;


/**
 * Provides instantiation service for actors objects of type {@link Actor}.<br/>
 * Actor's creation follows this process:
 * <ul>
 * <li>Loading the reactor class and checking validity</li>
 * <li>Parsing and loading configuration, checking for validity</li>
 * <li>Creating the actor's {@link io.amaze.bench.client.runtime.orchestrator.OrchestratorClient}</li>
 * <li>Injecting reactor's dependencies and instantiation</li>
 * <li>Returns a {@link Actor} object wrapper for the resource manager to use</li>
 * </ul>
 *
 * @see Actor
 * @see Reactor
 */
public final class ActorFactory {

    private static final ConfigParseOptions DEFAULT_CONFIG_PARSE_OPTIONS = //
            ConfigParseOptions.defaults() //
                    .setSyntax(ConfigSyntax.JSON) //
                    .setAllowMissing(true);

    private final OrchestratorClientFactory clientFactory;
    private final ConfigParseOptions configParseOptions;
    private final String agent;

    public ActorFactory(@NotNull final String agent, @NotNull final OrchestratorClientFactory clientFactory) {
        this.agent = checkNotNull(agent);
        this.clientFactory = checkNotNull(clientFactory);

        configParseOptions = DEFAULT_CONFIG_PARSE_OPTIONS;
    }

    public final Actor create(@NotNull final String name,
                              @NotNull final String className,
                              @NotNull final String jsonConfig) throws ValidationException {

        // Fail-fast
        Class<? extends Reactor> clazz = ActorValidators.get().loadAndValidate(className);
        Config config = parseConfig(jsonConfig);

        Method beforeMethod = findAtMostOneAnnotatedMethod(clazz, Before.class);
        Method afterMethod = findAtMostOneAnnotatedMethod(clazz, After.class);

        MetricsSink metricsSink = MetricsSink.create();

        OrchestratorActor client = clientFactory.createForActor();
        Reactor reactor = createReactor(name, metricsSink, clazz, client, config);

        return new BaseActor(name, agent, metricsSink, beforeMethod, afterMethod, reactor, client);
    }

    private Config parseConfig(@NotNull final String jsonConfig) throws ValidationException {
        try {
            return ConfigFactory.parseString(jsonConfig, configParseOptions);
        } catch (ConfigException e) {
            throw ValidationException.create("Configuration error", e);
        }
    }

    /**
     * Inject dependencies to create a reactor.
     */
    private Reactor createReactor(final String actorName,
                                  final MetricsSink sink,
                                  final Class<? extends Reactor> clazz,
                                  final OrchestratorActor client,
                                  final Config config) {

        MutablePicoContainer pico = new DefaultPicoContainer();

        pico.addComponent(config);

        pico.addComponent((Sender) (to, message) -> {
            checkNotNull(to);
            checkNotNull(message);

            client.sendToActor(to, new Message<>(actorName, message));
        });

        pico.addComponent((MetricsCollector) sink::add);

        pico.addComponent(clazz);

        return pico.getComponent(clazz);
    }


}
