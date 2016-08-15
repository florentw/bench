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
package io.amaze.bench.client.runtime.agent;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/3/16.
 */
public final class AgentInputMessage implements Serializable {

    private final String destinationAgent;
    private final Action action;
    private final Serializable data;

    public AgentInputMessage(@NotNull final String destinationAgent,
                             @NotNull final Action action,
                             @NotNull final Serializable data) {

        this.destinationAgent = checkNotNull(destinationAgent);
        this.action = checkNotNull(action);
        this.data = checkNotNull(data);
    }

    public Action getAction() {
        return action;
    }

    public Serializable getData() {
        return data;
    }

    public String getDestinationAgent() {
        return destinationAgent;
    }

    public enum Action {
        CREATE_ACTOR, //
        CLOSE_ACTOR //
    }
}
