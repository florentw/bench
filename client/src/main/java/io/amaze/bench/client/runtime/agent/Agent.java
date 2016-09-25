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
package io.amaze.bench.client.runtime.agent;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import io.amaze.bench.client.runtime.actor.ActorConfig;
import io.amaze.bench.client.runtime.actor.ActorManager;
import io.amaze.bench.client.runtime.actor.ActorManagers;
import io.amaze.bench.client.runtime.actor.ManagedActor;
import io.amaze.bench.client.runtime.cluster.AgentClusterClient;
import io.amaze.bench.client.runtime.cluster.ClusterClientFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.amaze.bench.client.runtime.actor.ActorLifecycleMessage.created;
import static io.amaze.bench.client.runtime.actor.ActorLifecycleMessage.failed;

/**
 * Created on 3/3/16.
 */
public class Agent implements AgentClientListener, AutoCloseable {

    static final String DEFAULT_AGENT_PREFIX = "agent-";
    private static final Logger LOG = LogManager.getLogger(Agent.class);
    private final Map<String, ManagedActor> actors = Maps.newHashMap();
    private final AgentClusterClient agentClient;
    private final ActorManager embeddedManager;
    private final ActorManager forkedManager;
    private final String name;

    public Agent(@NotNull final ClusterClientFactory clientFactory, @NotNull final ActorManagers actorManagers) {
        this(defaultName(), clientFactory, actorManagers);
    }

    public Agent(@NotNull final String name, @NotNull final ClusterClientFactory clientFactory,
                 @NotNull final ActorManagers actorManagers) {

        this.name = checkNotNull(name);
        checkNotNull(clientFactory);
        checkNotNull(actorManagers);

        AgentRegistrationMessage regMsg = AgentRegistrationMessage.create(name);

        LOG.info("{} Starting agent...", this);

        embeddedManager = actorManagers.createEmbedded(name, clientFactory);
        forkedManager = actorManagers.createForked(name);

        agentClient = clientFactory.createForAgent(name);

        agentClient.startAgentListener(name, this);
        sendRegistrationMessage(regMsg);

        LOG.info("{} Agent started.", this);
    }

    private static String defaultName() {
        String name = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@", "-");
        return DEFAULT_AGENT_PREFIX + name;
    }

    @Override
    public synchronized void onActorCreationRequest(@NotNull final ActorConfig actorConfig) {
        checkNotNull(actorConfig);

        String actorName = actorConfig.getName();
        if (actors.get(actorName) != null) {
            LOG.warn("{} The actor {} already exists.", this, actorName);
            return;
        }

        LOG.info("{} Actor creation request for actor {} with config: {}", this, actorName, actorConfig);

        Optional<ManagedActor> instance = createManagedActor(actorConfig);
        if (instance.isPresent()) {
            actors.put(actorName, instance.get());
            agentClient.sendToActorRegistry(created(actorName, name));
        }

        LOG.info("{} Actor {} created.", this, actorName);
    }

    @Override
    public synchronized void onActorCloseRequest(@NotNull final String actor) {
        checkNotNull(actor);

        ManagedActor found = actors.remove(actor);
        if (found == null) {
            LOG.warn("{} Could not find actor {} to close.", this, actor);
        } else {
            LOG.info("{} Closing actor {}...", this, actor);
            found.close();
            LOG.info("{} Closed actor {}.", this, actor);
        }
    }

    @Override
    public synchronized void close() throws Exception {
        LOG.info("{} Closing agent...", this);

        actors.values().forEach(ManagedActor::close);
        actors.clear();

        signOff();

        LOG.info("{} Agent closed.", this);
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "{\"Agent\":\"" + name + "\"" + "}";
    }

    @NotNull
    @VisibleForTesting
    synchronized Set<ManagedActor> getActors() {
        return ImmutableSet.copyOf(actors.values());
    }

    private void sendRegistrationMessage(@NotNull final AgentRegistrationMessage regMsg) {
        agentClient.sendToAgentRegistry(AgentLifecycleMessage.created(regMsg));
    }

    private void signOff() {
        agentClient.sendToAgentRegistry(AgentLifecycleMessage.closed(name));
    }

    private Optional<ManagedActor> createManagedActor(@NotNull final ActorConfig actorConfig) {
        try {
            ActorManager manager = actorManager(actorConfig);
            return Optional.of(manager.createActor(actorConfig));

        } catch (Exception e) {
            LOG.warn("Could not create actor with config {}.", actorConfig, e);
            actorFailure(actorConfig.getName(), e);
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

    private void actorFailure(@NotNull final String actor, @NotNull final Throwable throwable) {
        agentClient.sendToActorRegistry(failed(actor, throwable));
    }
}
