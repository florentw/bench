package io.amaze.bench.client.runtime.orchestrator;

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.shared.jms.FFMQClient;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

/**
 * Created on 3/3/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
abstract class JMSOrchestratorClient implements OrchestratorClient {

    private final JMSClient client;

    @VisibleForTesting
    JMSOrchestratorClient(@NotNull final JMSClient client) {
        this.client = checkNotNull(client);
    }

    JMSOrchestratorClient(@NotNull final String host, @NotNull final int port) {
        checkNotNull(host);
        checkArgument(port > 0);

        try {
            client = new FFMQClient(host, port);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    final JMSClient getClient() {
        return client;
    }

    @Override
    public final void sendToActor(@NotNull final String to, @NotNull final Message<? extends Serializable> message) {
        checkNotNull(to);
        checkNotNull(message);

        try {
            client.sendToQueue(to, message);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }

    @Override
    public final void close() {
        client.close();
    }

}
