package io.amaze.bench.client.runtime.actor;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/4/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class ActorInputMessage implements Serializable {

    private final Command command;
    private final String from;
    private final Serializable payload;

    public ActorInputMessage(@NotNull final Command command,
                             @NotNull final String from,
                             @NotNull final Serializable payload) {

        this.command = checkNotNull(command);
        this.from = checkNotNull(from);
        this.payload = checkNotNull(payload);
    }

    public Command getCommand() {
        return command;
    }

    public String getFrom() {
        return from;
    }

    public Serializable getPayload() {
        return payload;
    }

    public enum Command {
        INIT,
        CLOSE,
        DUMP_METRICS,
        MESSAGE
    }
}
