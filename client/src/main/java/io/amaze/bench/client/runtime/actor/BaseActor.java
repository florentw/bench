package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.api.ReactorException;
import io.amaze.bench.client.api.actor.Reactor;
import io.amaze.bench.client.runtime.agent.MasterOutputMessage;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClient;
import io.amaze.bench.shared.metric.Metric;
import io.amaze.bench.shared.metric.MetricsSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.amaze.bench.client.runtime.actor.ActorLifecycleMessage.Phase;
import static io.amaze.bench.client.runtime.agent.Constants.MASTER_ACTOR_NAME;
import static io.amaze.bench.client.runtime.agent.Constants.METRICS_ACTOR_NAME;
import static io.amaze.bench.client.runtime.agent.MasterOutputMessage.Action.ACTOR_LIFECYCLE;

/**
 * Created on 2/28/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class BaseActor implements Actor {

    private static final Logger LOG = LoggerFactory.getLogger(BaseActor.class);
    private static final String MSG_AFTER_METHOD_FAILED = " Error while invoking after method.";

    private final String name;
    private final String agentName;
    private final MetricsSink sink;
    private final Method beforeMethod;
    private final Method afterMethod;
    private final Reactor instance;
    private final OrchestratorClient client;

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public BaseActor(@NotNull final String name, @NotNull final String agentName,
                     @NotNull final MetricsSink sink,
                     final Method beforeMethod,
                     final Method afterMethod,
                     @NotNull final Reactor instance,
                     @NotNull final OrchestratorClient client) {

        this.name = name;
        this.agentName = agentName;
        this.sink = sink;
        this.beforeMethod = beforeMethod;
        this.afterMethod = afterMethod;
        this.instance = instance;
        this.client = client;

        // Plug the reactor listener to the JMS queue
        // Should be done last!
        client.startActorListener(this);
    }

    Reactor getInstance() {
        return instance;
    }

    @Override
    public void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug(this + " Initializing...");
        }

        if (beforeMethod == null) {
            sendLifecycleMessage(Phase.INITIALIZED);
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

        sendLifecycleMessage(Phase.INITIALIZED);
    }

    @Override
    public void onMessage(@NotNull final String from, @NotNull final Serializable message) {
        try {
            instance.onMessage(from, message);
        } catch (ReactorException e) {
            try {
                after();
            } catch (InvocationTargetException | IllegalAccessException afterException) {
                LOG.warn(this + MSG_AFTER_METHOD_FAILED, afterException);
            }
            actorFailure(e);
        }
    }

    @Override
    public final void dumpAndFlushMetrics() {
        Map<String, Metric> metrics = sink.getMetricsAndFlush();
        client.sendToActor(METRICS_ACTOR_NAME, new Message<>(name, (Serializable) metrics));
    }

    @Override
    public void close() {
        boolean running = this.isClosed.compareAndSet(false, true);
        if (!running) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(this + " Invoking close...");
        }

        try {
            if (!tryToCallAfterMethod()) {
                return;
            }

            sendLifecycleMessage(Phase.CLOSED);
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

    private void sendToActorRegistry(MasterOutputMessage.Action action, Serializable msg) {
        MasterOutputMessage masterOutputMessage = new MasterOutputMessage(action, msg);
        client.sendToActor(MASTER_ACTOR_NAME, new Message<>(name, masterOutputMessage));
    }

    private void sendLifecycleMessage(@NotNull final Phase phase) {
        ActorLifecycleMessage msg = new ActorLifecycleMessage(name, agentName, phase);
        sendToActorRegistry(ACTOR_LIFECYCLE, msg);
    }

    private void actorFailure(@NotNull final Throwable throwable) {
        LOG.warn(this + " failure.", throwable);

        ActorLifecycleMessage msg = new ActorLifecycleMessage(name, agentName, Phase.FAILED, throwable);
        sendToActorRegistry(ACTOR_LIFECYCLE, msg);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "Actor{" +
                "'" + name + '\'' +
                '}';
    }
}
