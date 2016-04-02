package io.amaze.bench.orchestrator;

import io.amaze.bench.client.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import io.amaze.bench.client.runtime.agent.MasterOutputMessage;
import io.amaze.bench.orchestrator.registry.ActorRegistryListener;
import io.amaze.bench.orchestrator.registry.AgentRegistryListener;
import io.amaze.bench.shared.jms.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;

/**
 * Created on 3/28/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class JMSMasterMessageListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(JMSMasterMessageListener.class);

    private final AgentRegistryListener agentsListener;
    private final ActorRegistryListener actorsListener;

    JMSMasterMessageListener(@NotNull final AgentRegistryListener agentsListener,
                             @NotNull final ActorRegistryListener actorsListener) {
        this.agentsListener = agentsListener;
        this.actorsListener = actorsListener;
    }

    @Override
    public void onMessage(final Message message) {
        MasterOutputMessage received;

        try {
            received = (MasterOutputMessage) JMSHelper.objectFromMsg((BytesMessage) message);
        } catch (Exception e) {
            LOG.error("Error while reading JMS message as MasterOutputMessage.", e);
            return;
        }

        switch (received.getAction()) { // NOSONAR
            case REGISTER_AGENT:
                onAgentRegistration(received);
                break;
            case UNREGISTER_AGENT:
                onAgentSignOff(received);
                break;
            case ACTOR_LIFECYCLE:
                onActorLifecycle(received);
                break;
        }
    }

    private void onActorLifecycle(final MasterOutputMessage received) {
        ActorLifecycleMessage lfMsg = (ActorLifecycleMessage) received.getData();
        String actor = lfMsg.getActor();

        switch (lfMsg.getPhase()) { // NOSONAR
            case CREATED:
                actorsListener.onActorCreated(actor, lfMsg.getAgent());
                break;
            case STARTED:
                actorsListener.onActorStarted(actor, lfMsg.getAgent());
                break;
            case FAILED:
                actorsListener.onActorFailed(actor, lfMsg.getThrowable());
                break;
            case CLOSED:
                actorsListener.onActorClosed(actor);
                break;
        }
    }

    private void onAgentSignOff(final MasterOutputMessage received) {
        String actorName = (String) received.getData();
        agentsListener.onAgentSignOff(actorName);
    }

    private void onAgentRegistration(final MasterOutputMessage received) {
        AgentRegistrationMessage regMsg = (AgentRegistrationMessage) received.getData();
        agentsListener.onAgentRegistration(regMsg);
    }
}
