package io.amaze.bench.client.runtime.agent;

/**
 * Message sent by an agent to the master when stopping.
 * <p/>
 * Created on 3/4/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class AgentSignOffMessage {

    private static final String SIGN_OFF_COMMAND = "Bye";

    public static String create() {
        return SIGN_OFF_COMMAND;
    }

}
