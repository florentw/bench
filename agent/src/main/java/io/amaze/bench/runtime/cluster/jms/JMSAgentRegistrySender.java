package io.amaze.bench.runtime.cluster.jms;

import io.amaze.bench.runtime.agent.AgentLifecycleMessage;
import io.amaze.bench.runtime.cluster.AgentRegistrySender;
import io.amaze.bench.runtime.message.Message;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.runtime.agent.Constants.AGENT_REGISTRY_TOPIC;

/**
 * Created on 10/18/16.
 */
public final class JMSAgentRegistrySender implements AgentRegistrySender {

    private final JMSClient client;
    private final String agent;

    public JMSAgentRegistrySender(@NotNull final JMSClient client, @NotNull final String agent) {
        this.client = checkNotNull(client);
        this.agent = checkNotNull(agent);
    }

    @Override
    public void send(@NotNull final AgentLifecycleMessage message) {
        checkNotNull(message);

        try {
            client.sendToTopic(AGENT_REGISTRY_TOPIC, new Message<>(agent, message));
        } catch (JMSException e) {
            throw propagate(e);
        }
    }
}
