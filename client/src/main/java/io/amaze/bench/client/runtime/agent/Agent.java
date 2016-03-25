package io.amaze.bench.client.runtime.agent;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import io.amaze.bench.client.api.ReactorException;
import io.amaze.bench.client.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.client.runtime.actor.ActorManager;
import io.amaze.bench.client.runtime.actor.ManagedActor;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClient;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import static io.amaze.bench.client.runtime.actor.ActorLifecycleMessage.Phase;
import static io.amaze.bench.client.runtime.agent.Constants.MASTER_ACTOR_NAME;
import static io.amaze.bench.client.runtime.agent.MasterOutputMessage.Action.*;

/**
 * Created on 3/3/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class Agent implements AgentClientListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(Agent.class);

    private final Map<String, ManagedActor> actors = Maps.newHashMap();
    private final OrchestratorClient agentClient;
    private final ActorManager manager;
    private final String name;

    Agent(@NotNull final OrchestratorClientFactory clientFactory,
          @NotNull final ActorManager actorManager) throws ReactorException {

        manager = actorManager;
        AgentRegistrationMessage regMsg = AgentRegistrationMessage.create();
        name = regMsg.getName();

        LOG.info("Starting agent \"" + name + "\"...");

        agentClient = clientFactory.createForAgent();

        agentClient.startAgentListener(name, Constants.AGENTS_ACTOR_NAME, this);
        sendRegistrationMessage(regMsg);

        LOG.info("Agent \"" + name + "\" started.");
    }

    @Override
    public synchronized void onActorCreationRequest(@NotNull final String actor,
                                                    @NotNull final String className,
                                                    @NotNull final String jsonConfig) {
        if (actors.get(actor) != null) {
            LOG.warn("The actor \"" + actor + "\" already exists.");
            return;
        }

        LOG.info("Actor creation request for actor \"" + actor + "\" with className \"" + className + "\", config: " + jsonConfig);

        ManagedActor instance = createManagedActor(actor, className, jsonConfig);
        if (instance != null) {
            actors.put(actor, instance);
            sendActorLifecycleMessage(actor, Phase.CREATED);
        }

        LOG.info("Actor \"" + actor + "\" created.");
    }

    @Override
    public synchronized void onActorCloseRequest(@NotNull final String actor) {
        ManagedActor found = actors.remove(actor);
        if (found == null) {
            LOG.warn("Could not find actor \"" + actor + "\" to close.");
        } else {
            LOG.info("Closing actor \"" + actor + "\"...");
            found.close();
            LOG.info("Closed actor \"" + actor + "\".");
        }
    }

    @Override
    public synchronized void close() throws Exception {
        LOG.info("Closing agent \"" + name + "\"...");

        for (ManagedActor actor : actors.values()) {
            actor.close();
        }
        actors.clear();

        signOffAgent();

        LOG.info("Agent \"" + name + "\" closed.");
    }

    @NotNull
    synchronized Set<ManagedActor> getActors() {
        return ImmutableSet.copyOf(actors.values());
    }

    private void sendRegistrationMessage(@NotNull final AgentRegistrationMessage regMsg) {
        sendToMaster(REGISTER_AGENT, regMsg);
    }

    private void signOffAgent() {
        sendToMaster(UNREGISTER_AGENT, AgentSignOffMessage.create());
    }

    private void sendActorLifecycleMessage(@NotNull final String actor, @NotNull final Phase phase) {
        ActorLifecycleMessage msg = new ActorLifecycleMessage(actor, phase);
        sendToMaster(ACTOR_LIFECYCLE, msg);
    }

    private void sendToMaster(MasterOutputMessage.Action action, Serializable msg) {
        MasterOutputMessage masterOutputMessage = new MasterOutputMessage(action, msg);
        agentClient.sendToActor(MASTER_ACTOR_NAME, new Message<>(name, masterOutputMessage));
    }

    private ManagedActor createManagedActor(@NotNull final String actor,
                                            @NotNull final String className,
                                            @NotNull final String jsonConfig) {
        ManagedActor instance;
        try {
            instance = manager.createActor(actor, className, jsonConfig);
        } catch (Exception e) {
            LOG.warn("Could not create actor \"" + actor + "\".", e);
            actorFailure(actor, e);
            return null;
        }
        return instance;
    }

    private void actorFailure(@NotNull final String actor, @NotNull final Throwable throwable) {
        ActorLifecycleMessage msg = new ActorLifecycleMessage(actor, Phase.FAILED, throwable);
        sendToMaster(ACTOR_LIFECYCLE, msg);
    }

}
