package io.amaze.bench.client.runtime.orchestrator;

/**
 * Created on 3/3/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class JMSOrchestratorClientFactory implements OrchestratorClientFactory {

    private final String jmsServerHost;
    private final int jmsServerPort;

    public JMSOrchestratorClientFactory(String jmsServerHost, int port) {
        this.jmsServerHost = jmsServerHost;
        this.jmsServerPort = port;
    }

    @Override
    public OrchestratorClient createForAgent() {
        return createClient();
    }

    @Override
    public OrchestratorClient createForActor() {
        return createClient();
    }

    private OrchestratorClient createClient() {
        return new JMSOrchestratorClient(jmsServerHost, jmsServerPort);
    }
}

