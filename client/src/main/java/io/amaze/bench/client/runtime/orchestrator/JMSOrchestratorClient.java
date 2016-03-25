package io.amaze.bench.client.runtime.orchestrator;

import com.google.common.base.Throwables;
import io.amaze.bench.client.runtime.actor.Actor;
import io.amaze.bench.client.runtime.agent.AgentClientListener;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.shared.jms.FFMQClient;
import io.amaze.bench.shared.jms.JMSClient;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created on 3/3/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class JMSOrchestratorClient implements OrchestratorClient {

    private final JMSClient client;

    JMSOrchestratorClient(@NotNull final String host, @NotNull final int port) {
        try {
            client = new FFMQClient(host, port);
        } catch (NamingException | JMSException e) {
            throw Throwables.propagate(e); // FIXME TYPED ?
        }
    }

    @Override
    public void startAgentListener(@NotNull final String agentName,
                                   @NotNull final String agentsTopicName,
                                   @NotNull final AgentClientListener listener) {
        try {
            client.addTopicListener(agentsTopicName, new JMSAgentMessageListener(agentName, listener));
            client.startListening();
        } catch (NamingException | JMSException e) {
            throw Throwables.propagate(e); // FIXME TYPED ?
        }
    }

    @Override
    public void startActorListener(@NotNull final Actor actor) {
        try {
            client.addQueueListener(actor.name(), new JMSActorMessageListener(actor));
        } catch (NamingException | JMSException e) {
            throw Throwables.propagate(e); // FIXME TYPED ?
        }
    }

    @Override
    public void sendToActor(@NotNull final String to, @NotNull final Message<? extends Serializable> message) {
        try {
            client.sendToQueue(to, message);
        } catch (NamingException | JMSException | IOException |
                InterruptedException | ClassNotFoundException e) {
            Throwables.propagate(e); // FIXME TYPED ?
        }
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

}
