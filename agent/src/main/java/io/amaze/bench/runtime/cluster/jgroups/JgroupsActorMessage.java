package io.amaze.bench.runtime.cluster.jgroups;

import io.amaze.bench.runtime.actor.ActorInputMessage;
import io.amaze.bench.runtime.actor.ActorKey;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/27/16.
 */
public final class JgroupsActorMessage implements Serializable {

    private final ActorKey to;
    private final ActorInputMessage inputMessage;

    public JgroupsActorMessage(final ActorKey to, final ActorInputMessage inputMessage) {
        this.to = checkNotNull(to);
        this.inputMessage = checkNotNull(inputMessage);
    }

    public ActorKey to() {
        return to;
    }

    public ActorInputMessage inputMessage() {
        return inputMessage;
    }
}
