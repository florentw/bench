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
package io.amaze.bench.leader;

import com.google.common.util.concurrent.SettableFuture;
import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.actor.ActorConfig;
import io.amaze.bench.cluster.actor.ActorDeployInfo;
import io.amaze.bench.cluster.actor.ActorInputMessage;
import io.amaze.bench.cluster.actor.ActorSender;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.cluster.registry.ActorRegistryListener;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.concurrent.Future;

import static java.util.Objects.requireNonNull;

/**
 * Created on 9/12/16.
 */
public final class Actors {

    private final ActorSender actorSender;
    private final ResourceManager resourceManager;
    private final ActorRegistry actorRegistry;

    public Actors(@NotNull final ActorSender actorSender,
                  @NotNull final ResourceManager resourceManager,
                  @NotNull final ActorRegistry actorRegistry) {

        this.actorSender = requireNonNull(actorSender);
        this.resourceManager = requireNonNull(resourceManager);
        this.actorRegistry = requireNonNull(actorRegistry);
    }

    public ActorHandle create(@NotNull final ActorConfig actorConfig) {
        requireNonNull(actorConfig);
        HandleRegistryListener handleRegistryListener = new HandleRegistryListener(actorSender, actorConfig);
        actorRegistry.addListener(handleRegistryListener);
        return handleRegistryListener.createActor();
    }

    public final class ActorHandle {
        private final SettableFuture<ActorConfig> actorCreated = SettableFuture.create();
        private final SettableFuture<ActorDeployInfo> actorInitialized = SettableFuture.create();
        private final SettableFuture<Throwable> actorFailed = SettableFuture.create();
        private final SettableFuture<Void> actorClosed = SettableFuture.create();

        private final ActorSender actorSender;
        private final ActorConfig config;

        ActorHandle(final ActorSender actorSender, final ActorConfig config) {
            this.actorSender = actorSender;
            this.config = config;
        }

        public void dumpMetrics() {
            actorSender.send(config.getKey(), ActorInputMessage.dumpMetrics());
        }

        public void bootstrap() {
            actorSender.send(config.getKey(), ActorInputMessage.bootstrap());
        }

        public void send(final String from, final Serializable message) {
            actorSender.send(config.getKey(), ActorInputMessage.message(from, message));
        }

        public Future<Void> close() {
            actorSender.send(config.getKey(), ActorInputMessage.close());
            return actorClosed;
        }

        public Future<ActorConfig> actorCreation() {
            return actorCreated;
        }

        public Future<ActorDeployInfo> actorInitialization() {
            return actorInitialized;
        }

        public Future<Throwable> actorFailure() {
            return actorFailed;
        }

        public Future<Void> actorTermination() {
            return actorClosed;
        }
    }

    private final class HandleRegistryListener implements ActorRegistryListener {
        private final ActorConfig config;
        private final ActorHandle handle;

        HandleRegistryListener(final ActorSender actorSender, final ActorConfig config) {
            this.config = config;
            this.handle = new ActorHandle(actorSender, config);
        }

        @Override
        public void onActorCreated(@NotNull final ActorKey key, @NotNull final AgentKey agent) {
            if (key.equals(config.getKey())) {
                handle.actorCreated.set(config);
            }
        }

        @Override
        public void onActorInitialized(@NotNull final ActorKey key, @NotNull final ActorDeployInfo deployInfo) {
            if (key.equals(config.getKey())) {
                handle.actorInitialized.set(deployInfo);
            }
        }

        @Override
        public void onActorFailed(@NotNull final ActorKey key, @NotNull final Throwable throwable) {
            if (key.equals(config.getKey())) {
                handle.actorFailed.set(throwable);

                handle.actorCreated.setException(throwable);
                handle.actorInitialized.setException(throwable);
                handle.actorClosed.setException(throwable);

                actorRegistry.removeListener(this);
            }
        }

        @Override
        public void onActorClosed(@NotNull final ActorKey key) {
            if (key.equals(config.getKey())) {
                handle.actorClosed.set(null);

                actorRegistry.removeListener(this);
            }
        }

        ActorHandle createActor() {
            resourceManager.createActor(config);
            return handle;
        }
    }
}
