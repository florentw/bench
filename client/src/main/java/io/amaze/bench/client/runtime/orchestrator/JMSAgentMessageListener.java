package io.amaze.bench.client.runtime.orchestrator;

import io.amaze.bench.client.api.actor.Reactor;
import io.amaze.bench.client.runtime.agent.AgentClientListener;
import io.amaze.bench.client.runtime.agent.AgentInputMessage;
import io.amaze.bench.shared.jms.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.BytesMessage;
import javax.jms.MessageListener;

/**
 * Convert received JMS messages from the master to the agent to calls to its {@link Reactor} methods.
 */
class JMSAgentMessageListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(JMSAgentMessageListener.class);

    private final AgentClientListener listener;
    private final String agentName;

    JMSAgentMessageListener(final String agentName, final AgentClientListener listener) {
        this.agentName = agentName;
        this.listener = listener;
    }

    @Override
    public void onMessage(final javax.jms.Message message) {
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
        listener.onActorCreationRequest(data.getActor(), data.getClassName(), data.getJsonConfig());
    }
}
