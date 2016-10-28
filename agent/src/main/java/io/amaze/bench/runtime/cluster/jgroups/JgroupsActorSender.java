package io.amaze.bench.runtime.cluster.jgroups;

import io.amaze.bench.runtime.actor.ActorInputMessage;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.cluster.ActorSender;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.RegisteredActor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/18/16.
 */
public final class JgroupsActorSender implements ActorSender {

    private static final Logger log = LogManager.getLogger();

    private final JgroupsSender sender;
    private final ActorRegistry actorRegistry;

    public JgroupsActorSender(@NotNull final JgroupsSender sender, @NotNull final ActorRegistry actorRegistry) {
        this.sender = checkNotNull(sender);
        this.actorRegistry = checkNotNull(actorRegistry);
    }

    @Override
    public void send(@NotNull final ActorKey to, @NotNull final ActorInputMessage message) {
        checkNotNull(to);
        checkNotNull(message);
        log.debug("Sending {} to {}", message, to);

        // We need to resolve the endpoint using the actor's name first
        RegisteredActor registeredActor = actorRegistry.byKey(to);
        if (registeredActor == null || registeredActor.getDeployInfo() == null) {
            throw new NoSuchElementException("Cannot find endpoint for " + to + ".");
        }

        JgroupsActorMessage jgroupsMsg = new JgroupsActorMessage(to, message);
        sender.sendToEndpoint(registeredActor.getDeployInfo().getEndpoint(), jgroupsMsg);
    }
}
