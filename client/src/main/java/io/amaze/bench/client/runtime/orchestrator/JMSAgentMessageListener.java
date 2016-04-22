package io.amaze.bench.client.runtime.orchestrator;

import io.amaze.bench.client.api.actor.Reactor;
import io.amaze.bench.client.runtime.agent.AgentClientListener;
import io.amaze.bench.client.runtime.agent.AgentInputMessage;
import io.amaze.bench.shared.jms.JMSHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.BytesMessage;
import javax.jms.MessageListener;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Convert received JMS messages from the master to the agent to calls to its {@link Reactor} methods.
 */
final class JMSAgentMessageListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(JMSAgentMessageListener.class);

    private final AgentClientListener listener;
    private final String agentName;

    JMSAgentMessageListener(@NotNull final String agentName, @NotNull final AgentClientListener listener) {
        this.agentName = checkNotNull(agentName);
        this.listener = checkNotNull(listener);
    }

    @Override
    public void onMessage(@NotNull final javax.jms.Message message) {
        checkNotNull(message);

        AgentInputMessage msg;
        try {
            msg = (AgentInputMessage) JMSHelper.objectFromMsg((BytesMessage) message);
        } catch (Exception e) {
            LOG.error("Invalid AgentInputMessage received, message:" + message, e);
            return;
        }

        // Process messages only if sent to myself (topic)
        if (!msg.getDestinationAgent().equals(agentName)) {
            return;
        }

        switch (msg.getAction()) { // NOSONAR
            case CREATE_ACTOR:
                createActor(msg);
                break;
            case CLOSE_ACTOR:
                closeActor(msg);
                break;
        }
    }

    private void closeActor(final AgentInputMessage msg) {
        String aToClose = (String) msg.getData();
        listener.onActorCloseRequest(aToClose);
    }

    private void createActor(final AgentInputMessage msg) {
        ActorCreationRequest data = (ActorCreationRequest) msg.getData();
        listener.onActorCreationRequest(data.getActorConfig());
    }
}
