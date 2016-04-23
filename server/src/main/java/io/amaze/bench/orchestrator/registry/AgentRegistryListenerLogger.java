package io.amaze.bench.orchestrator.registry;

import io.amaze.bench.client.runtime.agent.AgentRegistrationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/30/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class AgentRegistryListenerLogger implements AgentRegistryListener {

    private static final Logger LOG = LoggerFactory.getLogger(AgentRegistryListenerLogger.class);

    private final AgentRegistryListener delegate;

    AgentRegistryListenerLogger(@NotNull final AgentRegistryListener delegate) {
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public void onAgentRegistration(@NotNull final AgentRegistrationMessage msg) {
        checkNotNull(msg);

        LOG.info(String.format("Agent \"%s\" registering, msg: %s", msg.getName(), msg));

        delegate.onAgentRegistration(msg);
    }

    @Override
    public void onAgentSignOff(@NotNull final String agent) {
        checkNotNull(agent);

        LOG.info(String.format("Agent \"%s\" signing off.", agent));

        delegate.onAgentSignOff(agent);
    }
}
