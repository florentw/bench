package io.amaze.bench.runtime.cluster.jgroups;

import io.amaze.bench.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.runtime.cluster.ActorRegistrySender;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/18/16.
 */
public final class JgroupsActorRegistrySender implements ActorRegistrySender {

    private final JgroupsSender jgroupsSender;

    public JgroupsActorRegistrySender(@NotNull final JgroupsSender jgroupsSender) {
        this.jgroupsSender = checkNotNull(jgroupsSender);
    }

    @Override
    public void send(@NotNull final ActorLifecycleMessage actorLifecycleMessage) {
        checkNotNull(actorLifecycleMessage);

        jgroupsSender.broadcast(actorLifecycleMessage);
    }
}
