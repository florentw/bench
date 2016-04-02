package io.amaze.bench.orchestrator.registry;

/**
 * Created on 3/29/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class RegisteredActor {

    private final String actor;
    private final String agent;
    private final State state;

    RegisteredActor(final String actor, final String agent, final State state) {
        this.actor = actor;
        this.agent = agent;
        this.state = state;
    }

    public String getName() {
        return actor;
    }

    public State getState() {
        return state;
    }

    public String getAgent() {
        return agent;
    }

    enum State {
        CREATED,
        STARTED
    }
}
