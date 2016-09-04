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

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.client.runtime.actor.ActorConfig;
import io.amaze.bench.client.runtime.agent.AgentInputMessage;
import io.amaze.bench.client.runtime.agent.AgentInputMessage.Action;
import io.amaze.bench.client.runtime.orchestrator.ActorCreationRequest;
import io.amaze.bench.orchestrator.registry.AgentRegistry;
import io.amaze.bench.orchestrator.registry.RegisteredAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 4/2/16.
 */
public final class ResourceManager implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceManager.class);

    private final OrchestratorServer orchestratorServer;
    private final AgentRegistry agentRegistry;

    private final Map<String, RegisteredAgent> actorsToAgents = new ConcurrentHashMap<>();
    private final Random rand = new Random();

    public ResourceManager(@NotNull final OrchestratorServer orchestratorServer,
                           @NotNull final AgentRegistry agentRegistry) {
        this.orchestratorServer = checkNotNull(orchestratorServer);
        this.agentRegistry = checkNotNull(agentRegistry);
    }

    /**
     * Requests an actor instantiation using the given {@link ActorConfig} configuration.
     *
     * @param actorConfig The actor configuration.
     * @throws IllegalStateException if the agent cannot be created before
     */
    public void createActor(@NotNull final ActorConfig actorConfig) {
        checkNotNull(actorConfig);

        String name = actorConfig.getName();
        orchestratorServer.createActorQueue(name);

        ActorCreationRequest actorCreationMsg = new ActorCreationRequest(actorConfig);

        Optional<RegisteredAgent> agent = applyDeployStrategy(actorConfig);
        if (!agent.isPresent()) {
            throw new IllegalStateException("No agents found to create actor " + name);
        }

        AgentInputMessage msg = new AgentInputMessage(agent.get().getName(), Action.CREATE_ACTOR, actorCreationMsg);

        orchestratorServer.sendToAgent(msg);
        actorsToAgents.put(name, agent.get());
    }

    /**
     * Requests to close an actor. It will contact the agent hosting the actor in order for it to close it.
     *
     * @param name The name of the actor to close.
     * @throws IllegalArgumentException if the actor does not exist.
     */
    public void closeActor(@NotNull final String name) {
        checkNotNull(name);

        RegisteredAgent agent = actorsToAgents.remove(name);
        if (agent == null) {
            throw new IllegalArgumentException("Attempt to close unknown actor " + name);
        }

        orchestratorServer.deleteActorQueue(name);
        orchestratorServer.sendToAgent(new AgentInputMessage(agent.getName(), Action.CLOSE_ACTOR, name));
    }

    /**
     * Closes every actor handled by this ResourceManager
     */
    @Override
    public void close() {
        Set<String> actorsMapCopy = new HashSet<>(actorsToAgents.keySet());
        actorsMapCopy.forEach(this::closeActor);
    }

    @VisibleForTesting
    Map<String, RegisteredAgent> getActorsToAgents() {
        return Collections.unmodifiableMap(actorsToAgents);
    }

    private Optional<RegisteredAgent> pickRandomAgent(final Set<RegisteredAgent> agents) {
        int index = rand.nextInt(agents.size());
        Iterator<RegisteredAgent> it = agents.iterator();
        for (int i = 0; i < index; i++) {
            it.next();
        }
        return Optional.of(it.next());
    }

    /**
     * Assigns actor creation to a specific agent according to the following strategy:
     * <ul>
     * <li>If the actor is configured with preferred hosts, pick one randomly</li>
     * <li>Otherwise pick a random agent amongst all registered agents</li>
     * </ul>
     *
     * @param actorConfig Configuration of the actor to assign an agent to
     * @return An optional agent to host the actor
     */
    private Optional<RegisteredAgent> applyDeployStrategy(final ActorConfig actorConfig) {
        Set<RegisteredAgent> allAgents = agentRegistry.all();

        if (allAgents.isEmpty()) {
            return Optional.empty();
        }

        final List<String> preferredHosts = actorConfig.getDeployConfig().getPreferredHosts();
        if (preferredHosts.isEmpty()) {
            return pickRandomAgent(allAgents);
        }

        // Try to find an agent on one of the preferred hosts
        Optional<RegisteredAgent> agentOnPreferredHost = pickAgentOnOneOfPreferredHosts(allAgents, preferredHosts);
        if (agentOnPreferredHost.isPresent()) {
            return agentOnPreferredHost;
        } else {
            LOG.warn("Could not find an agent deployed on one of the preferred hosts: " + preferredHosts);

            // Fallback to pick a random agent
            return pickRandomAgent(allAgents);
        }
    }

    private Optional<RegisteredAgent> pickAgentOnOneOfPreferredHosts(final Set<RegisteredAgent> allAgents,
                                                                     final List<String> preferredHosts) {
        return allAgents.stream().filter(input -> preferredHosts.contains(input.getSystemConfig().getHostName())).findFirst();
    }
}
