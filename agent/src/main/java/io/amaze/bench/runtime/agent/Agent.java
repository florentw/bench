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
package io.amaze.bench.runtime.agent;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import io.amaze.bench.Endpoint;
import io.amaze.bench.cluster.ClusterClientFactory;
import io.amaze.bench.cluster.actor.ActorConfig;
import io.amaze.bench.cluster.actor.ActorKey;
import io.amaze.bench.cluster.agent.*;
import io.amaze.bench.runtime.actor.ActorManager;
import io.amaze.bench.runtime.actor.ActorManagers;
import io.amaze.bench.runtime.actor.ManagedActor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.io.Closeable;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.amaze.bench.cluster.actor.ActorLifecycleMessage.created;
import static io.amaze.bench.cluster.actor.ActorLifecycleMessage.failed;

/**
 * Created on 3/3/16.
 */
public class Agent implements AgentClientListener, Closeable {

    private static final String DEFAULT_AGENT_PREFIX = "agent-";
    private static final Logger log = LogManager.getLogger();

    private final Map<ActorKey, ManagedActor> actors = Maps.newHashMap();
    private final AgentClusterClient agentClient;
    private final ActorManager embeddedManager;
    private final ActorManager forkedManager;

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final AgentKey key;

    public Agent(@NotNull final ClusterClientFactory clientFactory, @NotNull final ActorManagers actorManagers) {
        this(defaultKey(), clientFactory, actorManagers);
    }

    public Agent(@NotNull final AgentKey key, //
                 @NotNull final ClusterClientFactory clientFactory, //
                 @NotNull final ActorManagers actorManagers) {

        this.key = checkNotNull(key);
        Endpoint endpoint = clientFactory.localEndpoint();
        checkNotNull(clientFactory);
        checkNotNull(actorManagers);

        AgentRegistrationMessage regMsg = AgentRegistrationMessage.create(key, endpoint);

        log.info("{} Starting...", this);

        embeddedManager = actorManagers.createEmbedded(key, clientFactory);
        forkedManager = actorManagers.createForked(key, clientFactory.clusterConfigFactory());

        agentClient = clientFactory.createForAgent(key);

        agentClient.startAgentListener(key, this);
        sendRegistrationMessage(regMsg);

        log.info("{} Started.", this);
    }

    private static AgentKey defaultKey() {
        String suffix = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@", "-");
        return new AgentKey(DEFAULT_AGENT_PREFIX + suffix);
    }

    @Override
    public synchronized void onActorCreationRequest(@NotNull final ActorConfig actorConfig) {
        checkNotNull(actorConfig);

        ActorKey actorKey = actorConfig.getKey();
        if (actors.get(actorKey) != null) {
            log.warn("{} The actor {} already exists.", this, actorKey);
            return;
        }

        log.info("{} Actor creation request for {} with config: {}", this, actorKey, actorConfig);

        Optional<ManagedActor> instance = createManagedActor(actorConfig);
        if (instance.isPresent()) {
            actors.put(actorKey, instance.get());
        }

        log.info("{} Actor {} created.", this, actorKey);
    }

    @Override
    public synchronized void onActorCloseRequest(@NotNull final ActorKey actor) {
        checkNotNull(actor);

        ManagedActor found = actors.remove(actor);
        if (found == null) {
            log.warn("{} Could not find {} to close.", this, actor);
        } else {
            log.info("{} Closing {}...", this, actor);
            found.close();
            log.info("{} Closed {}.", this, actor);
        }
    }

    @Override
    public synchronized void close() {
        if (!isClosed.compareAndSet(false, true)) {
            return;
        }

        log.info("{} Closing...", this);

        actors.values().forEach(ManagedActor::close);
        actors.clear();

        signOff();

        forkedManager.close();
        embeddedManager.close();

        agentClient.close();

        log.info("{} Closed.", this);
    }

    @NotNull
    public AgentKey getKey() {
        return key;
    }

    @Override
    public String toString() {
        return key.toString();
    }

    @NotNull
    @VisibleForTesting
    synchronized Set<ManagedActor> getActors() {
        return ImmutableSet.copyOf(actors.values());
    }

    private void sendRegistrationMessage(@NotNull final AgentRegistrationMessage regMsg) {
        agentClient.agentRegistrySender().send(AgentLifecycleMessage.created(regMsg));
    }

    private void signOff() {
        agentClient.agentRegistrySender().send(AgentLifecycleMessage.closed(key));
    }

    private Optional<ManagedActor> createManagedActor(@NotNull final ActorConfig actorConfig) {
        try {
            ActorManager manager = actorManager(actorConfig);
            agentClient.actorRegistrySender().send(created(actorConfig.getKey(), key));
            return Optional.of(manager.createActor(actorConfig));

        } catch (Exception e) {
            log.warn("Could not create actor with config {}.", actorConfig, e);
            actorFailure(actorConfig.getKey(), e);
            return Optional.empty();
        }
    }

    private ActorManager actorManager(@NotNull final ActorConfig actorConfig) {
        if (actorConfig.getDeployConfig().isForked()) {
            return forkedManager;
        } else {
            return embeddedManager;
        }
    }

    private void actorFailure(@NotNull final ActorKey actor, @NotNull final Throwable throwable) {
        agentClient.actorRegistrySender().send(failed(actor, throwable));
    }
}
