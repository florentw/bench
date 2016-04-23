package io.amaze.bench.client.runtime.agent;

import io.amaze.bench.client.runtime.actor.Actor;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorActor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 3/30/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class RecorderOrchestratorActor implements OrchestratorActor {

    private final Map<String, List<Message<? extends Serializable>>> sentMessages = new HashMap<>();

    private boolean actorListenerStarted = false;

    @Override
    public void startActorListener(@NotNull final Actor actor) {
        actorListenerStarted = true;
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

    boolean isActorListenerStarted() {
        return actorListenerStarted;
    }

    public Map<String, List<Message<? extends Serializable>>> getSentMessages() {
        return sentMessages;
    }
}
