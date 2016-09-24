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
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.client.runtime.actor.metric.MetricValue;
import io.amaze.bench.client.runtime.actor.metric.MetricsInternal;
import io.amaze.bench.client.runtime.cluster.ActorClusterClient;
import io.amaze.bench.client.runtime.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.json.SystemInfo;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.amaze.bench.client.runtime.actor.ActorLifecycleMessage.*;
import static io.amaze.bench.client.runtime.agent.Constants.METRICS_ACTOR_NAME;

/**
 * Created on 2/28/16.
 */
public class BaseActor implements RuntimeActor {

    private static final Logger LOG = LoggerFactory.getLogger(BaseActor.class);
    private static final String MSG_AFTER_METHOD_FAILED = " Error while invoking after method.";

    private final String name;
    private final MetricsInternal metrics;
    private final Method beforeMethod;
    private final Method afterMethod;
    private final Reactor instance;
    private final ActorClusterClient client;

    private final AtomicBoolean running = new AtomicBoolean(true);

    public BaseActor(@NotNull final String name,
                     @NotNull final MetricsInternal metrics,
                     @NotNull final Reactor instance, @NotNull final ActorClusterClient client,
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
        if (LOG.isDebugEnabled()) {
            LOG.debug(this + " Initializing...");
        }

        if (beforeMethod == null) {
            sendLifecycleMessage(initialized(name, deployInfo()));
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(this + " Invoking before method...");
        }
        try {
            beforeMethod.invoke(instance);
        } catch (Exception e) {
            LOG.warn(this + " Error while invoking before method on actor \"" + name + "\".", e);
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
            Map<Metric, List<MetricValue>> metricValues = this.metrics.dumpAndFlush();
            client.sendToActor(METRICS_ACTOR_NAME, new Message<>(name, (Serializable) metricValues));
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
            /*
             * Recoverable exception, the Reactor code is supposed to be fine, just log the exception.
             */
            LOG.warn(this + " Recoverable exception caught on message:" + message + ", from:" + from, e);

        } catch (TerminationException ignored) { // NOSONAR
            /*
             * This is a graceful termination, just perform a regular close on the actor.
             */
            close();

        } catch (RuntimeException | ReactorException e) { // Irrecoverable exceptions
            /*
             * In the case of a non-recoverable error of the actor, we need to clean-up by calling after,
             * and notifying the failure.
             */
            try {
                after();
            } catch (InvocationTargetException | IllegalAccessException afterException) {
                LOG.warn(this + MSG_AFTER_METHOD_FAILED, afterException);
            }

            actorFailure(e);
        }
    }

    @Override
    public void close() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(this + " Invoking close...");
        }

        try {
            if (!tryToCallAfterMethod()) {
                return;
            }

            sendLifecycleMessage(closed(name));
        } catch (Exception e) {
            LOG.info(this + " Exception while closing.", e);
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                LOG.warn(this + " Error while closing client.", e);
            }
        }
    }

    @Override
    public String toString() {
        return "{\"Actor\":{" + "\"name\":\"" + name + "\"" + "}}";
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
            if (LOG.isDebugEnabled()) {
                LOG.debug(this + " Invoking after method...");
            }
            afterMethod.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOG.warn(this + MSG_AFTER_METHOD_FAILED, e);
            throw e;
        }
    }

    private void sendLifecycleMessage(@NotNull final ActorLifecycleMessage msg) {
        client.sendToActorRegistry(msg);
    }

    private void actorFailure(@NotNull final Throwable throwable) {
        LOG.warn(this + " failure.", throwable);

        client.sendToActorRegistry(failed(name, throwable));
    }
}
