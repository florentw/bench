package io.amaze.bench.client.runtime.orchestrator;

import io.amaze.bench.client.runtime.actor.ActorConfig;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Message sent by the master to an agent when it is required to createForAgent an actor.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class ActorCreationRequest implements Serializable {

    private final ActorConfig actorConfig;

    public ActorCreationRequest(@NotNull final ActorConfig actorConfig) {
        this.actorConfig = checkNotNull(actorConfig);
    }

    ActorConfig getActorConfig() {
        return actorConfig;
    }
}
