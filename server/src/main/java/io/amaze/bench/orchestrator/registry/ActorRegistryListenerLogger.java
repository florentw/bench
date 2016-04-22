package io.amaze.bench.orchestrator.registry;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/29/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class ActorRegistryListenerLogger implements ActorRegistryListener {

    private static final Logger LOG = LoggerFactory.getLogger(ActorRegistryListenerLogger.class);

    private final ActorRegistryListener delegate;

    ActorRegistryListenerLogger(@NotNull final ActorRegistryListener delegate) {
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public void onActorCreated(@NotNull final String name, @NotNull final String agent) {
        checkNotNull(name);
        checkNotNull(agent);

        LOG.info(String.format("Create actor \"%s\" by agent \"%s\"", name, agent));

        delegate.onActorCreated(name, agent);
    }

    @Override
    public void onActorInitialized(@NotNull final String name, @NotNull final String agent) {
        checkNotNull(name);
        checkNotNull(agent);

        LOG.info(String.format("Initialization of actor \"%s\" by agent \"%s\"", name, agent));

        delegate.onActorInitialized(name, agent);
    }

    @Override
    public void onActorFailed(@NotNull final String name, @NotNull final Throwable throwable) {
        checkNotNull(name);
        checkNotNull(throwable);

        LOG.info(String.format("Actor failure of actor \"%s\"", name), throwable);

        delegate.onActorFailed(name, throwable);
    }

    @Override
    public void onActorClosed(@NotNull final String name) {
        checkNotNull(name);

        LOG.info(String.format("Close actor \"%s\"", name));

        delegate.onActorClosed(name);
    }
}
