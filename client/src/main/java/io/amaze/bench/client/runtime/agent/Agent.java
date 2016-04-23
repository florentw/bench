package io.amaze.bench.client.runtime.agent;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import io.amaze.bench.client.runtime.actor.*;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClient;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.amaze.bench.client.runtime.actor.ActorLifecycleMessage.Phase;
import static io.amaze.bench.client.runtime.agent.Constants.MASTER_ACTOR_NAME;
import static io.amaze.bench.client.runtime.agent.MasterOutputMessage.Action.*;
import static java.lang.String.format;

/**
 * Created on 3/3/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class Agent implements AgentClientListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(Agent.class);

    private final Map<String, ManagedActor> actors = Maps.newHashMap();
    private final OrchestratorClient agentClient;
    private final ActorManager embeddedManager;
    private final ActorManager forkedManager;

    private final String name;

    public Agent(@NotNull final OrchestratorClientFactory clientFactory,
                 @NotNull final ActorManagerFactory actorManagers) {
        checkNotNull(clientFactory);
        checkNotNull(actorManagers);

        AgentRegistrationMessage regMsg = AgentRegistrationMessage.create();
        name = regMsg.getName();

        LOG.info(format("Starting agent \"%s\"...", name));

        embeddedManager = actorManagers.createEmbedded(name, clientFactory);
        forkedManager = actorManagers.createForked(name);

        agentClient = clientFactory.createForAgent();

        agentClient.startAgentListener(name, this);
        sendRegistrationMessage(regMsg);

        LOG.info(format("Agent \"%s\" started.", name));
    }

    @Override
    public synchronized void onActorCreationRequest(@NotNull final ActorConfig actorConfig) {
        checkNotNull(actorConfig);

        String actorName = actorConfig.getName();
        if (actors.get(actorName) != null) {
            LOG.warn(format("The actor \"%s\" already exists.", actorName));
            return;
        }

        LOG.info(format("Actor creation request for actor \"%s\" with config: %s", actorName, actorConfig));

        ManagedActor instance = createManagedActor(actorConfig);
        if (instance != null) {
            actors.put(actorName, instance);
            sendActorLifecycleMessage(actorName, Phase.CREATED);
        }

        LOG.info(format("Actor \"%s\" created.", actorName));
    }

    @Override
    public synchronized void onActorCloseRequest(@NotNull final String actor) {
        checkNotNull(actor);

        ManagedActor found = actors.remove(actor);
        if (found == null) {
            LOG.warn(format("Could not find actor \"%s\" to close.", actor));
        } else {
            LOG.info(format("Closing actor \"%s\"...", actor));
            found.close();
            LOG.info(format("Closed actor \"%s\".", actor));
        }
    }

    @Override
    public synchronized void close() throws Exception {
        LOG.info(format("Closing agent \"%s\"...", name));

        for (ManagedActor actor : actors.values()) {
            actor.close();
        }
        actors.clear();

        signOffAgent();

        LOG.info(format("Agent \"%s\" closed.", name));
    }

    @NotNull
    synchronized Set<ManagedActor> getActors() {
        return ImmutableSet.copyOf(actors.values());
    }

    @NotNull
    public String getName() {
        return name;
    }

    private void sendRegistrationMessage(@NotNull final AgentRegistrationMessage regMsg) {
        sendToMaster(REGISTER_AGENT, regMsg);
    }

    private void signOffAgent() {
        sendToMaster(UNREGISTER_AGENT, name);
    }

    private void sendActorLifecycleMessage(@NotNull final String actor, @NotNull final Phase phase) {
        ActorLifecycleMessage msg = new ActorLifecycleMessage(actor, name, phase);
        sendToMaster(ACTOR_LIFECYCLE, msg);
    }

    private void sendToMaster(MasterOutputMessage.Action action, Serializable msg) {
        MasterOutputMessage masterOutputMessage = new MasterOutputMessage(action, msg);
        agentClient.sendToActor(MASTER_ACTOR_NAME, new Message<>(name, masterOutputMessage));
    }

    private ManagedActor createManagedActor(@NotNull final ActorConfig actorConfig) {
        ManagedActor instance;
        try {
            ActorManager manager = getActorManager(actorConfig.getDeployConfig());
            instance = manager.createActor(actorConfig);

        } catch (Exception e) {
            LOG.warn(format("Could not create actor with config \"%s\".", actorConfig), e);
            actorFailure(actorConfig.getName(), e);
            return null;
        }
        return instance;
    }

    private ActorManager getActorManager(@NotNull final DeployConfig deployConfig) {
        if (deployConfig.isForked()) {
            return forkedManager;
        } else {
            return embeddedManager;
        }
    }

    private void actorFailure(@NotNull final String actor, @NotNull final Throwable throwable) {
        ActorLifecycleMessage msg = new ActorLifecycleMessage(actor, name, Phase.FAILED, throwable);
        sendToMaster(ACTOR_LIFECYCLE, msg);
    }

}
