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
package io.amaze.bench.cluster.registry;

import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.actor.ActorDeployInfo;
import io.amaze.bench.cluster.agent.AgentKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Decorator to log actor registry events
 */
final class ActorRegistryListenerLogger implements ActorRegistryListener {

    private static final Logger log = LogManager.getLogger();

    private final ActorRegistryListener delegate;

    ActorRegistryListenerLogger(@NotNull final ActorRegistryListener delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public void onActorCreated(@NotNull final ActorKey key, @NotNull final AgentKey agent) {
        requireNonNull(key);
        requireNonNull(agent);

        log.info("Create actor {} by agent {}", key, agent);

        delegate.onActorCreated(key, agent);
    }

    @Override
    public void onActorInitialized(@NotNull final ActorKey key, @NotNull final ActorDeployInfo deployInfo) {
        requireNonNull(key);
        requireNonNull(deployInfo);

        log.info("Initialization of actor {} with deploy info {}", key, deployInfo);

        delegate.onActorInitialized(key, deployInfo);
    }

    @Override
    public void onActorFailed(@NotNull final ActorKey key, @NotNull final Throwable throwable) {
        requireNonNull(key);
        requireNonNull(throwable);

        log.info("Actor failure of actor {}", key, throwable);

        delegate.onActorFailed(key, throwable);
    }

    @Override
    public void onActorClosed(@NotNull final ActorKey key) {
        requireNonNull(key);

        log.info("Close actor {}", key);

        delegate.onActorClosed(key);
    }
}
