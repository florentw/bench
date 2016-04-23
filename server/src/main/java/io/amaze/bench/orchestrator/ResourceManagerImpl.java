package io.amaze.bench.orchestrator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
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
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class ResourceManagerImpl implements ResourceManager {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceManagerImpl.class);

    private final OrchestratorServer orchestratorServer;
    private final AgentRegistry agentRegistry;

    private final Map<String, RegisteredAgent> actorsToAgents = new ConcurrentHashMap<>();
    private final Random rand = new Random();

    ResourceManagerImpl(@NotNull final OrchestratorServer orchestratorServer,
                        @NotNull final AgentRegistry agentRegistry) {
        this.orchestratorServer = checkNotNull(orchestratorServer);
        this.agentRegistry = checkNotNull(agentRegistry);
    }

    @Override
    public void createActor(@NotNull final ActorConfig actorConfig) {
        checkNotNull(actorConfig);

        String name = actorConfig.getName();
        orchestratorServer.createActorQueue(name);

        ActorCreationRequest actorCreationMsg = new ActorCreationRequest(actorConfig);

        RegisteredAgent agent = applyDeployStrategy(actorConfig);
        if (agent == null) {
            throw new IllegalStateException("No agents found to create actor " + name);
        }

        AgentInputMessage msg = new AgentInputMessage(agent.getName(), Action.CREATE_ACTOR, actorCreationMsg);

        orchestratorServer.sendToAgent(msg);
        actorsToAgents.put(name, agent);
    }

    @Override
    public void closeActor(@NotNull final String name) {
        checkNotNull(name);

        RegisteredAgent agent = actorsToAgents.remove(name);
        if (agent == null) {
            throw new IllegalArgumentException("Attempt to close unknown actor " + name);
        }

        orchestratorServer.deleteActorQueue(name);
        orchestratorServer.sendToAgent(new AgentInputMessage(agent.getName(), Action.CLOSE_ACTOR, name));
    }

    @Override
    public void close() {
        Set<String> actorsMapCpy = new HashSet<>(actorsToAgents.keySet());
        for (String actor : actorsMapCpy) {
            closeActor(actor);
        }
    }

    @VisibleForTesting
    Map<String, RegisteredAgent> getActorsToAgents() {
        return Collections.unmodifiableMap(actorsToAgents);
    }

    private RegisteredAgent pickRandomAgent(final Set<RegisteredAgent> agents) {
        int index = rand.nextInt(agents.size());
        Iterator<RegisteredAgent> it = agents.iterator();
        for (int i = 0; i < index; i++) {
            it.next();
        }
        return it.next();
    }

    private RegisteredAgent applyDeployStrategy(final ActorConfig actorConfig) {
        Set<RegisteredAgent> allAgents = agentRegistry.all();

        if (allAgents.isEmpty()) {
            return null;
        }

        final List<String> preferredHosts = actorConfig.getDeployConfig().getPreferredHosts();
        if (preferredHosts.isEmpty()) {
            return pickRandomAgent(allAgents);
        }

        // Try to find an agent on one of the preferred hosts
        RegisteredAgent agentOnPreferredHost = pickAgentOnOneOfPreferredHosts(allAgents, preferredHosts);
        if (agentOnPreferredHost != null) {
            return agentOnPreferredHost;
        } else {
            // Fallback to pick a random agent
            return pickRandomAgent(allAgents);
        }
    }

    private RegisteredAgent pickAgentOnOneOfPreferredHosts(final Set<RegisteredAgent> allAgents,
                                                           final List<String> preferredHosts) {
        Optional<RegisteredAgent> agentMatchingHost = FluentIterable.from(allAgents).filter(new Predicate<RegisteredAgent>() {
            @Override
            public boolean apply(final RegisteredAgent input) {
                return preferredHosts.contains(input.getSystemInfo().getHostName());
            }
        }).first();

        if (agentMatchingHost.isPresent()) {
            return agentMatchingHost.get();
        }

        LOG.warn("Could not find an agent deployed on one of the preferred hosts: " + preferredHosts);
        return null;
    }
}
