package io.amaze.bench.client.runtime.agent;

import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorAgent;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 4/24/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class RecorderOrchestratorAgent implements OrchestratorAgent {

    private final Map<String, List<Message<? extends Serializable>>> sentMessages = new HashMap<>();

    private boolean agentListenerStarted = false;

    @Override
    public void startAgentListener(@NotNull final String agent, @NotNull final AgentClientListener listener) {
        agentListenerStarted = true;
    }

    @Override
    public void sendToActor(@NotNull final String to, @NotNull final Message<? extends Serializable> message) {
        List<Message<? extends Serializable>> msgs = sentMessages.get(to);
        if (msgs == null) {
            msgs = new ArrayList<>();
            sentMessages.put(to, msgs);
        }
        msgs.add(message);
    }

    @Override
    public void close() {
        // Dummy
    }

    public Map<String, List<Message<? extends Serializable>>> getSentMessages() {
        return sentMessages;
    }

    boolean isAgentListenerStarted() {
        return agentListenerStarted;
    }
}
