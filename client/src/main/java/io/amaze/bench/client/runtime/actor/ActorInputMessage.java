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
package io.amaze.bench.client.runtime.actor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/4/16.
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
        INIT, //
        CLOSE, //
        DUMP_METRICS, //
        MESSAGE //
    }
}
