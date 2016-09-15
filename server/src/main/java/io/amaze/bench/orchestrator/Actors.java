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
package io.amaze.bench.orchestrator;

import com.google.common.util.concurrent.SettableFuture;
import io.amaze.bench.client.runtime.actor.ActorConfig;
import io.amaze.bench.orchestrator.registry.ActorRegistry;
import io.amaze.bench.orchestrator.registry.ActorRegistryListener;

import javax.validation.constraints.NotNull;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 9/12/16.
 */
public final class Actors {

    private final ResourceManager resourceManager;
    private final ActorRegistry actorRegistry;

    public Actors(@NotNull final ResourceManager resourceManager, @NotNull final ActorRegistry actorRegistry) {
        this.resourceManager = checkNotNull(resourceManager);
        this.actorRegistry = checkNotNull(actorRegistry);
    }

    public ActorHandle create(ActorConfig actorConfig) {
        checkNotNull(actorConfig);
        HandleRegistryListener handleRegistryListener = new HandleRegistryListener(actorConfig);
        actorRegistry.addListener(handleRegistryListener);
        return handleRegistryListener.createActor();
    }

    public final class ActorHandle {
        private final SettableFuture<ActorConfig> actorCreated = SettableFuture.create();
        private final SettableFuture<ActorConfig> actorInitialized = SettableFuture.create();
        private final SettableFuture<Throwable> actorFailed = SettableFuture.create();
        private final SettableFuture<ActorConfig> actorClosed = SettableFuture.create();

        public Future<ActorConfig> actorCreation() {
            return actorCreated;
        }

        public Future<ActorConfig> actorInitialization() {
            return actorInitialized;
        }

        public Future<Throwable> actorFailure() {
            return actorFailed;
        }

        public Future<ActorConfig> actorTermination() {
            return actorClosed;
        }
    }

    private final class HandleRegistryListener implements ActorRegistryListener {
        private final ActorConfig config;
        private final ActorHandle handle;

        HandleRegistryListener(final ActorConfig config) {
            this.config = config;
            this.handle = new ActorHandle();
        }

        @Override
        public void onActorCreated(@NotNull final String name, @NotNull final String agent) {
            if (name.equals(config.getName())) {
                handle.actorCreated.set(config);
            }
        }

        @Override
        public void onActorInitialized(@NotNull final String name, @NotNull final String agent) {
            if (name.equals(config.getName())) {
                handle.actorInitialized.set(config);
            }
        }

        @Override
        public void onActorFailed(@NotNull final String name, @NotNull final Throwable throwable) {
            if (name.equals(config.getName())) {
                handle.actorFailed.set(throwable);

                handle.actorCreated.setException(throwable);
                handle.actorInitialized.setException(throwable);
                handle.actorClosed.setException(throwable);
            }
        }

        @Override
        public void onActorClosed(@NotNull final String name) {
            if (name.equals(config.getName())) {
                handle.actorClosed.set(config);
            }
        }

        ActorHandle createActor() {
            resourceManager.createActor(config);
            return handle;
        }
    }
}
