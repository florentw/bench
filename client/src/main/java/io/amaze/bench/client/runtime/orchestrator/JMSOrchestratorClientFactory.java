package io.amaze.bench.client.runtime.orchestrator;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/3/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class JMSOrchestratorClientFactory implements OrchestratorClientFactory {

    private final String jmsServerHost;
    private final int jmsServerPort;

    public JMSOrchestratorClientFactory(@NotNull final String jmsServerHost, final int port) {
        checkNotNull(jmsServerHost);
        checkArgument(port > 0);

        this.jmsServerHost = jmsServerHost;
        this.jmsServerPort = port;
    }

    @Override
    public OrchestratorAgent createForAgent() {
        return new JMSOrchestratorAgent(jmsServerHost, jmsServerPort);
    }

    @Override
    public OrchestratorActor createForActor() {
        return new JMSOrchestratorActor(jmsServerHost, jmsServerPort);
    }

}

