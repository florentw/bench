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
package io.amaze.bench.cluster.registry;

import io.amaze.bench.client.runtime.actor.ActorDeployInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Decorator to log actor registry events
 */
final class ActorRegistryListenerLogger implements ActorRegistryListener {

    private static final Logger LOG = LogManager.getLogger(ActorRegistryListenerLogger.class);

    private final ActorRegistryListener delegate;

    ActorRegistryListenerLogger(@NotNull final ActorRegistryListener delegate) {
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public void onActorCreated(@NotNull final String name, @NotNull final String agent) {
        checkNotNull(name);
        checkNotNull(agent);

        LOG.info("Create actor {} by agent {}", name, agent);

        delegate.onActorCreated(name, agent);
    }

    @Override
    public void onActorInitialized(@NotNull final String name, @NotNull final ActorDeployInfo deployInfo) {
        checkNotNull(name);
        checkNotNull(deployInfo);

        LOG.info("Initialization of actor {} with deploy info {}", name, deployInfo);

        delegate.onActorInitialized(name, deployInfo);
    }

    @Override
    public void onActorFailed(@NotNull final String name, @NotNull final Throwable throwable) {
        checkNotNull(name);
        checkNotNull(throwable);

        LOG.info("Actor failure of actor {}", name, throwable);

        delegate.onActorFailed(name, throwable);
    }

    @Override
    public void onActorClosed(@NotNull final String name) {
        checkNotNull(name);

        LOG.info("Close actor {}", name);

        delegate.onActorClosed(name);
    }
}
