package io.amaze.bench.runtime.cluster.jgroups;

import io.amaze.bench.runtime.agent.AgentLifecycleMessage;
import io.amaze.bench.runtime.cluster.AgentRegistrySender;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/18/16.
 */
public final class JgroupsAgentRegistrySender implements AgentRegistrySender {

    private final JgroupsSender jgroupsSender;

    public JgroupsAgentRegistrySender(@NotNull final JgroupsSender jgroupsSender) {
        this.jgroupsSender = checkNotNull(jgroupsSender);
    }

    @Override
    public void send(@NotNull final AgentLifecycleMessage message) {
        checkNotNull(message);

        jgroupsSender.broadcast(message);
    }
}
