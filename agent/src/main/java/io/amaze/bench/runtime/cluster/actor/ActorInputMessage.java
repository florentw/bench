/*
 * Copyright 2016 Florent Weber <florent.weber@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.amaze.bench.runtime.cluster.actor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/4/16.
 */
public final class ActorInputMessage implements Serializable {

    private static final String NA = "n/a";

    private final Command command;
    private final String from;
    private final Serializable payload;

    private ActorInputMessage(@NotNull final Command command,
                              @NotNull final String from,
                              @NotNull final Serializable payload) {
        this.command = checkNotNull(command);
        this.from = checkNotNull(from);
        this.payload = checkNotNull(payload);

        checkArgument(!from.trim().isEmpty(), "From should not be empty.");
    }

    public static ActorInputMessage dumpMetrics() {
        return new ActorInputMessage(Command.DUMP_METRICS, NA, NA);
    }

    public static ActorInputMessage sendMessage(@NotNull final String from, @NotNull final Serializable payload) {
        return new ActorInputMessage(Command.MESSAGE, from, payload);
    }

    public static ActorInputMessage close() {
        return new ActorInputMessage(Command.CLOSE, NA, NA);
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

    @Override
    public int hashCode() {
        return Objects.hash(command, from, payload);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActorInputMessage that = (ActorInputMessage) o;
        return command == that.command && //
                Objects.equals(from, that.from) && //
                Objects.equals(payload, that.payload);
    }

    @Override
    public String toString() {
        return "{\"ActorInputMessage\":{" + //
                "\"command\":\"" + command + "\"" + ", " + //
                "\"from\":\"" + from + "\"" + ", " + //
                "\"payload\":\"" + payload + "\"}}";
    }

    public enum Command {
        CLOSE, //
        DUMP_METRICS, //
        MESSAGE
    }
}
