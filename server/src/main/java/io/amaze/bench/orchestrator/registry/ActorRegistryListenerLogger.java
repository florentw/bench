package io.amaze.bench.orchestrator.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/29/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class ActorRegistryListenerLogger implements ActorRegistryListener {

    private static final Logger LOG = LoggerFactory.getLogger(ActorRegistryListenerLogger.class);

    private final ActorRegistryListener delegate;

    ActorRegistryListenerLogger(@NotNull final ActorRegistryListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onActorCreated(@NotNull final String name, @NotNull final String agent) {
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("Create actor \"%s\" by agent \"%s\"", name, agent));
        }

        delegate.onActorCreated(name, agent);
    }

    @Override
    public void onActorStarted(@NotNull final String name, @NotNull final String agent) {
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("Start of actor \"%s\" by agent \"%s\"", name, agent));
        }

        delegate.onActorStarted(name, agent);
    }

    @Override
    public void onActorFailed(@NotNull final String name, @NotNull final Throwable throwable) {
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("Actor failure of actor \"%s\"", name), throwable);
        }

        delegate.onActorFailed(name, throwable);
    }

    @Override
    public void onActorClosed(@NotNull final String name) {
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("Close actor \"%s\"", name));
        }

        delegate.onActorClosed(name);
    }
}
