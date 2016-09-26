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

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.api.Reactor;
import io.amaze.bench.api.ReactorException;
import io.amaze.bench.api.RecoverableException;
import io.amaze.bench.api.TerminationException;
import io.amaze.bench.client.runtime.actor.metric.MetricValuesMessage;
import io.amaze.bench.client.runtime.actor.metric.MetricsInternal;
import io.amaze.bench.client.runtime.cluster.ActorClusterClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.json.SystemInfo;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.amaze.bench.client.runtime.actor.ActorLifecycleMessage.*;

/**
 * Created on 2/28/16.
 */
public class ActorInternal implements RuntimeActor {

    private static final Logger LOG = LogManager.getLogger(ActorInternal.class);
    private static final String MSG_AFTER_METHOD_FAILED = "{} Error while invoking after method.";

    private final String name;
    private final MetricsInternal metrics;
    private final Method beforeMethod;
    private final Method afterMethod;
    private final Reactor instance;
    private final ActorClusterClient client;

    private final AtomicBoolean running = new AtomicBoolean(true);

    public ActorInternal(@NotNull final String name,
                         @NotNull final MetricsInternal metrics,
                         @NotNull final Reactor instance,
                         @NotNull final ActorClusterClient client,
                         final Method beforeMethod,
                         final Method afterMethod) {

        this.name = checkNotNull(name);
        this.metrics = checkNotNull(metrics);
        this.beforeMethod = beforeMethod;
        this.afterMethod = afterMethod;
        this.instance = checkNotNull(instance);
        this.client = checkNotNull(client);

        // Plug the reactor listener to the JMS queue
        // Should be done last!
        client.startActorListener(this);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void init() {
        LOG.debug("{} Initializing...", this);

        if (beforeMethod == null) {
            sendLifecycleMessage(initialized(name, deployInfo()));
            return;
        }

        LOG.debug("{} Invoking before method...", this);
        try {
            beforeMethod.invoke(instance);
        } catch (Exception e) {
            LOG.warn("{} Error while invoking before method on actor {}.", this, name, e);
            try {
                after();
            } catch (InvocationTargetException | IllegalAccessException ignored) { // NOSONAR
            }
            actorFailure(e);
            return;
        }

        sendLifecycleMessage(initialized(name, deployInfo()));
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
        checkNotNull(from);
        checkNotNull(message);

        try {
            instance.onMessage(from, message);

        } catch (RecoverableException e) {
            //Recoverable exception, the Reactor code is supposed to be fine, just log the exception.
            LOG.warn("{} Recoverable exception caught on message:{}, from:{}", this, message, from, e);

        } catch (TerminationException ignored) { // NOSONAR
            // This is a graceful termination, just perform a regular close on the actor.
            close();

        } catch (RuntimeException | ReactorException e) {
            // In the case of a non-recoverable error of the actor, we need to clean-up by calling after,
            // and notifying the failure.
            try {
                after();
            } catch (InvocationTargetException | IllegalAccessException afterException) {
                LOG.warn(MSG_AFTER_METHOD_FAILED, this, afterException);
            }

            actorFailure(e);
        }
    }

    @Override
    public void close() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        LOG.debug("{} Invoking close...", this);

        try {
            if (!tryToCallAfterMethod()) {
                return;
            }

            sendLifecycleMessage(closed(name));
        } catch (Exception e) {
            LOG.info("{} Exception while closing.", this, e);
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                LOG.warn("{} Error while closing client.", this, e);
            }
        }
    }

    @Override
    public String toString() {
        return "{\"Actor\":\"" + name + "\"}";
    }

    @VisibleForTesting
    Reactor getInstance() {
        return instance;
    }

    private ActorDeployInfo deployInfo() {
        int pid = new SystemInfo().getOperatingSystem().getProcessId();
        return new ActorDeployInfo(pid);
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
            LOG.debug("{} Invoking after method...", this);

            afterMethod.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOG.warn(MSG_AFTER_METHOD_FAILED, this, e);
            throw e;
        }
    }

    private void sendLifecycleMessage(@NotNull final ActorLifecycleMessage msg) {
        client.sendToActorRegistry(msg);
    }

    private void actorFailure(@NotNull final Throwable throwable) {
        LOG.warn("{} failure.", this, throwable);

        client.sendToActorRegistry(failed(name, throwable));
    }
}
