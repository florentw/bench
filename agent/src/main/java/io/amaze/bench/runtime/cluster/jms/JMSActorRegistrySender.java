package io.amaze.bench.runtime.cluster.jms;

import io.amaze.bench.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.runtime.cluster.ActorRegistrySender;
import io.amaze.bench.runtime.message.Message;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static io.amaze.bench.runtime.agent.Constants.ACTOR_REGISTRY_TOPIC;

/**
 * Created on 10/18/16.
 */
public final class JMSActorRegistrySender implements ActorRegistrySender {

    private final JMSClient jmsClient;
    private final String from;

    public JMSActorRegistrySender(@NotNull final JMSClient jmsClient, @NotNull final String from) {
        this.jmsClient = checkNotNull(jmsClient);
        this.from = checkNotNull(from);
    }

    @Override
    public void send(@NotNull final ActorLifecycleMessage actorLifecycleMessage) {
        Message msg = new Message<>(from, actorLifecycleMessage);
        try {
            jmsClient.sendToTopic(ACTOR_REGISTRY_TOPIC, msg);
        } catch (JMSException e) {
            throw propagate(e);
        }
    }
}
