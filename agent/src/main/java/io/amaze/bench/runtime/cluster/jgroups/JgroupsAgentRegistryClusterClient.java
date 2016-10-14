package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.annotations.VisibleForTesting;
import io.amaze.bench.runtime.agent.AgentLifecycleMessage;
import io.amaze.bench.runtime.agent.Constants;
import io.amaze.bench.runtime.cluster.registry.AgentRegistry;
import io.amaze.bench.runtime.cluster.registry.AgentRegistryClusterClient;
import io.amaze.bench.runtime.cluster.registry.AgentRegistryListener;
import io.amaze.bench.shared.jgroups.*;
import org.jgroups.Address;
import org.jgroups.Message;

import javax.validation.constraints.NotNull;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Created on 10/14/16.
 */
public final class JgroupsAgentRegistryClusterClient implements AgentRegistryClusterClient {

    static final JgroupsStateKey AGENT_REGISTRY_STATE_KEY = new JgroupsStateKey(Constants.AGENT_REGISTRY_TOPIC);

    private final JgroupsListenerMultiplexer listenerMultiplexer;
    private final JgroupsStateMultiplexer stateMultiplexer;
    private final JgroupsViewMultiplexer viewMultiplexer;
    private final AgentRegistry agentRegistry;

    private JgroupsViewListener viewListener;

    public JgroupsAgentRegistryClusterClient(@NotNull final JgroupsListenerMultiplexer listenerMultiplexer,
                                             @NotNull final JgroupsStateMultiplexer stateMultiplexer,
                                             @NotNull final JgroupsViewMultiplexer viewMultiplexer,
                                             @NotNull final AgentRegistry agentRegistry) {

        this.listenerMultiplexer = checkNotNull(listenerMultiplexer);
        this.stateMultiplexer = checkNotNull(stateMultiplexer);
        this.viewMultiplexer = checkNotNull(viewMultiplexer);
        this.agentRegistry = checkNotNull(agentRegistry);

        stateMultiplexer.addStateHolder(stateHolder());
    }

    @Override
    public void startRegistryListener(@NotNull final AgentRegistryListener agentsListener) {
        checkNotNull(agentsListener);

        listenerMultiplexer.addListener(AgentLifecycleMessage.class, registryListener(agentsListener));
        viewListener = viewListener();
        viewMultiplexer.addListener(viewListener);
    }

    @Override
    public void close() {
        stateMultiplexer.removeStateHolder(AGENT_REGISTRY_STATE_KEY);

        // to be done only if startRegistryListener was called
        if (viewListener != null) {
            listenerMultiplexer.removeListenerFor(AgentLifecycleMessage.class);
            viewMultiplexer.removeListener(viewListener);
        }
    }

    private JgroupsListener<AgentLifecycleMessage> registryListener(final AgentRegistryListener agentsListener) {
        return new RegistryMessageListener(agentsListener);
    }

    private JgroupsStateHolder<?> stateHolder() {
        return new RegistryStateHolder(agentRegistry);
    }

    private JgroupsViewListener viewListener() {
        return new RegistryViewListener(agentRegistry);
    }

    @VisibleForTesting
    static final class RegistryViewListener implements JgroupsViewListener {
        private final AgentRegistry agentRegistry;

        RegistryViewListener(@NotNull final AgentRegistry agentRegistry) {
            this.agentRegistry = agentRegistry;
        }

        @Override
        public void initialView(final Collection<Address> members) {
            // Nothing to do here
        }

        @Override
        public void memberJoined(@NotNull final Address address) {
            // Nothing to do here
        }

        @Override
        public void memberLeft(@NotNull final Address address) {
            agentRegistry.onEndpointDisconnected(new JgroupsEndpoint(address));
        }
    }

    @VisibleForTesting
    static final class RegistryStateHolder implements JgroupsStateHolder<AgentView> {
        private final AgentRegistry agentRegistry;

        RegistryStateHolder(@NotNull final AgentRegistry agentRegistry) {
            this.agentRegistry = agentRegistry;
        }

        @Override
        public JgroupsStateKey getKey() {
            return AGENT_REGISTRY_STATE_KEY;
        }

        @Override
        public AgentView getState() {
            return new AgentView(agentRegistry.all());
        }

        @Override
        public void setState(@NotNull final AgentView newState) {
            checkNotNull(newState);
            agentRegistry.resetState(newState.getRegisteredAgents());
        }
    }

    @VisibleForTesting
    static final class RegistryMessageListener implements JgroupsListener<AgentLifecycleMessage> {
        private final AgentRegistryListener agentsListener;

        RegistryMessageListener(@NotNull final AgentRegistryListener agentsListener) {
            this.agentsListener = agentsListener;
        }

        @Override
        public void onMessage(@NotNull final Message msg, @NotNull final AgentLifecycleMessage agentMsg) {
            switch (agentMsg.getState()) {
                case CREATED:
                    onAgentRegistration(agentMsg);
                    break;
                case CLOSED:
                    onAgentSignOff(agentMsg);
                    break;
                case FAILED:
                    onAgentFailure(agentMsg);
                    break;
                default:
                    break;
            }
        }

        private void onAgentSignOff(final AgentLifecycleMessage received) {
            String agentName = received.getAgent();
            agentsListener.onAgentSignOff(agentName);
        }

        private void onAgentRegistration(final AgentLifecycleMessage received) {
            agentsListener.onAgentRegistration(received.getRegistrationMessage());
        }

        private void onAgentFailure(final AgentLifecycleMessage received) {
            agentsListener.onAgentFailed(received.getAgent(), received.getThrowable());
        }
    }
}
