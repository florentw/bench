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

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.api.*;
import io.amaze.bench.cluster.actor.ActorClusterClient;
import io.amaze.bench.cluster.actor.ActorDeployInfo;
import io.amaze.bench.cluster.actor.ActorLifecycleMessage;
import io.amaze.bench.cluster.actor.RuntimeActor;
import io.amaze.bench.cluster.metric.MetricValuesMessage;
import io.amaze.bench.runtime.actor.metric.MetricsInternal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.json.SystemInfo;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.amaze.bench.cluster.actor.ActorLifecycleMessage.*;
import static java.util.Objects.requireNonNull;

/**
 * Main internal implementation of the {@link RuntimeActor}.
 * It is a wrapper around the {@link Reactor} instance provided.<br/>
 * It uses an instance of {@link ActorClusterClient} to communicate with cluster members:
 * <ul>
 * <li>Will receive messages from other actors addressed to it on {@link #onMessage(String, Serializable)}</li>
 * <li>Send state notifications to the actor registry topic</li>
 * <li>Allows to send metrics to the metrics topic</li>
 * </ul>
 */
public class ActorInternal implements RuntimeActor {

    private static final Logger log = LogManager.getLogger();
    private static final String ERROR_INVOKING_METHOD = "{} Error while invoking {} method.";

    private final ActorKey actorKey;
    private final MetricsInternal metrics;
    private final Reactor<Serializable> instance;
    private final ActorClusterClient client;

    private final Method beforeMethod;
    private final Method afterMethod;
    private final Method bootstrapMethod;

    private final AtomicBoolean running = new AtomicBoolean(true);

    public ActorInternal(@NotNull final ActorKey actorKey,
                         @NotNull final MetricsInternal metrics,
                         @NotNull final Reactor<Serializable> instance,
                         @NotNull final ActorClusterClient client,
                         final Method beforeMethod,
                         final Method afterMethod,
                         final Method bootstrapMethod) {

        this.actorKey = requireNonNull(actorKey);
        this.metrics = requireNonNull(metrics);
        this.instance = requireNonNull(instance);
        this.client = requireNonNull(client);

        this.beforeMethod = beforeMethod;
        this.afterMethod = afterMethod;
        this.bootstrapMethod = bootstrapMethod;

        // Plug the reactor listener to the cluster messaging system
        // Should be done last!
        client.startActorListener(this);
    }

    @Override
    public ActorKey getKey() {
        return actorKey;
    }

    @Override
    public void init() {
        log.debug("{} Initializing...", this);

        if (beforeMethod == null) {
            sendLifecycleMessage(initialized(actorKey, deployInfo()));
            return;
        }

        log.debug("{} Invoking before method...", this);
        try {
            beforeMethod.invoke(instance);
        } catch (Exception e) { // NOSONAR - We want to catch everything here
            log.warn(ERROR_INVOKING_METHOD, this, "before", e);
            try {
                after();
            } catch (InvocationTargetException | IllegalAccessException ex) {
                log.debug(ERROR_INVOKING_METHOD, this, "before", ex);
            }
            actorFailure(e);
            return;
        }

        sendLifecycleMessage(initialized(actorKey, deployInfo()));
    }

    @Override
    public void bootstrap() {
        log.info("{} Bootstrapping scenario...", this);

        log.debug("{} Invoking bootstrap method...", this);
        try {
            if (bootstrapMethod == null) {
                throw new IllegalStateException(
                        "Bootstrap called on a Reactor class that does not declare a @Bootstrap method.");
            }

            bootstrapMethod.invoke(instance);
        } catch (Exception e) { // NOSONAR - We want to catch everything here
            log.warn(ERROR_INVOKING_METHOD, this, "bootstrap", e);
            try {
                after();
            } catch (InvocationTargetException | IllegalAccessException ex) {
                log.debug(ERROR_INVOKING_METHOD, this, "bootstrap", ex);
            }
            actorFailure(e);
        }
    }

    @Override
    public final void dumpAndFlushMetrics() {
        try {
            MetricValuesMessage metricValues = this.metrics.dumpAndFlush();
            client.sendMetrics(metricValues);
        } catch (RuntimeException e) {
            actorFailure(e);
        }
    }

    @Override
    public void onMessage(@NotNull final String from, @NotNull final Serializable message) {
        requireNonNull(from);
        requireNonNull(message);

        try {
            instance.onMessage(from, message);

        } catch (RecoverableException e) {
            //Recoverable exception, the Reactor code is supposed to be fine, just log the exception.
            log.warn("{} Recoverable exception caught on message:{}, from:{}", this, message, from, e);

        } catch (TerminationException ignored) { // NOSONAR
            // This is a graceful termination, just perform a regular close on the actor.
            close();

        } catch (RuntimeException | ReactorException e) {
            // In the case of a non-recoverable error of the actor, we need to clean-up by calling after,
            // and notifying the failure.
            try {
                after();
            } catch (InvocationTargetException | IllegalAccessException afterException) {
                log.warn(ERROR_INVOKING_METHOD, this, "after", afterException);
            }

            actorFailure(e);
        }
    }

    @Override
    public void close() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        log.debug("{} Invoking close...", this);

        try {
            if (!tryToCallAfterMethod()) {
                return;
            }

            sendLifecycleMessage(closed(actorKey));
        } catch (Exception e) { // NOSONAR - We want to catch everything here
            log.info("{} Exception while closing.", this, e);
        } finally {
            try {
                client.close();
            } catch (Exception e) { // NOSONAR - We want to catch everything
                log.warn("{} Error while closing client.", this, e);
            }
        }
    }

    @Override
    public String toString() {
        return actorKey.toString();
    }

    @VisibleForTesting
    public Reactor getInstance() {
        return instance;
    }

    private ActorDeployInfo deployInfo() {
        int pid = new SystemInfo().getOperatingSystem().getProcessId();
        return new ActorDeployInfo(client.localEndpoint(),
                                   pid,
                                   ManagementFactory.getRuntimeMXBean().getInputArguments());
    }

    private boolean tryToCallAfterMethod() {
        try {
            after();
        } catch (InvocationTargetException | IllegalAccessException e) {
            actorFailure(e);
            return false;
        }
        return true;
    }

    private void after() throws InvocationTargetException, IllegalAccessException {
        if (afterMethod == null) {
            return;
        }

        try {
            log.debug("{} Invoking after method...", this);

            afterMethod.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn(ERROR_INVOKING_METHOD, this, "after", e);
            throw e;
        }
    }

    private void sendLifecycleMessage(@NotNull final ActorLifecycleMessage msg) {
        client.actorRegistrySender().send(msg);
    }

    private void actorFailure(@NotNull final Throwable throwable) {
        log.warn("{} failure.", this, throwable);

        client.actorRegistrySender().send(failed(actorKey, throwable));
    }
}
