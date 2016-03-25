package io.amaze.bench.client.runtime.orchestrator;

import com.google.common.base.Preconditions;
import io.amaze.bench.client.api.actor.Reactor;
import io.amaze.bench.client.runtime.actor.Actor;
import io.amaze.bench.client.runtime.actor.ActorInputMessage;
import io.amaze.bench.shared.jms.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.BytesMessage;
import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;

/**
 * Convert received JMS messages for an actor to calls to its {@link Reactor} methods.
 */
class JMSActorMessageListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(JMSActorMessageListener.class);

    private final Actor actor;

    JMSActorMessageListener(@NotNull final Actor actor) {
        this.actor = actor;
    }

    @Override
    public void onMessage(final javax.jms.Message message) {
        ActorInputMessage msg;
        try {
            msg = (ActorInputMessage) JMSHelper.objectFromMsg((BytesMessage) message);
        } catch (Exception e) {
            LOG.error("Invalid ActorInputMessage received, message:" + message, e);
            return;
        }

        switch (msg.getCommand()) { // NOSONAR
            case START:
                actor.start();
                break;
            case STOP:
                actor.close();
                break;
            case DUMP_METRICS:
                actor.dumpMetrics();
                break;
            case MESSAGE:
                Preconditions.checkNotNull(msg.getPayload());
                actor.onMessage(msg.getFrom(), msg.getPayload());
                break;
        }
    }
}
