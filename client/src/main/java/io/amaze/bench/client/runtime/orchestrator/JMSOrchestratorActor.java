package io.amaze.bench.client.runtime.orchestrator;

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.client.runtime.actor.Actor;
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
final class JMSOrchestratorActor extends JMSOrchestratorClient implements OrchestratorActor {

    @VisibleForTesting
    JMSOrchestratorActor(@NotNull final JMSClient client) {
        super(client);
    }

    JMSOrchestratorActor(@NotNull final String host, @NotNull final int port) {
        super(host, port);
    }

    @Override
    public void startActorListener(@NotNull final Actor actor) {
        checkNotNull(actor);

        try {
            getClient().addQueueListener(actor.name(), new JMSActorMessageListener(actor));
            getClient().startListening();
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

}
