package io.amaze.bench.client.runtime.orchestrator;

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.client.runtime.agent.AgentClientListener;
import io.amaze.bench.client.runtime.agent.Constants;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

/**
 * Created on 4/24/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class JMSOrchestratorAgent extends JMSOrchestratorClient implements OrchestratorAgent {

    @VisibleForTesting
    JMSOrchestratorAgent(@NotNull final JMSClient client) {
        super(client);
    }

    JMSOrchestratorAgent(@NotNull final String host, @NotNull final int port) {
        super(host, port);
    }

    @Override
    public void startAgentListener(@NotNull final String agentName, @NotNull final AgentClientListener listener) {
        checkNotNull(agentName);
        checkNotNull(listener);

        try {
            getClient().addTopicListener(Constants.AGENTS_ACTOR_NAME, new JMSAgentMessageListener(agentName, listener));
            getClient().startListening();
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

}
