package io.amaze.bench.client.runtime.actor;

import com.typesafe.config.*;
import io.amaze.bench.client.api.*;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorActor;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;
import io.amaze.bench.shared.metric.Metric;
import io.amaze.bench.shared.metric.MetricsSink;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.amaze.bench.shared.helper.ReflectionHelper.findAtMostOneAnnotatedMethod;


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
 * @author Florent Weber (florent.weber@gmail.com)
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
        Config config;
        try {
            config = ConfigFactory.parseString(jsonConfig, configParseOptions);
        } catch (ConfigException e) {
            throw ValidationException.create("Configuration error", e);
        }
        return config;
    }

    /**
     * Inject dependencies to createForAgent a reactor.
     */
    private Reactor createReactor(final String actorName,
                                  final MetricsSink sink,
                                  final Class<? extends Reactor> clazz, final OrchestratorActor client,
                                  final Config config) {

        MutablePicoContainer pico = new DefaultPicoContainer();

        pico.addComponent(config);

        pico.addComponent(new Sender() {
            @Override
            public void send(@NotNull final String to, @NotNull final Serializable message) {
                checkNotNull(to);
                checkNotNull(message);

                client.sendToActor(to, new Message<>(actorName, message));
            }
        });

        pico.addComponent(new MetricsCollector() {
            @Override
            public void putMetric(@NotNull final String key, @NotNull final Metric metric) {
                sink.add(key, metric);
            }
        });

        pico.addComponent(clazz);

        return pico.getComponent(clazz);
    }


}
