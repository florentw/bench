/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
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
package io.amaze.bench.cluster.agent;

import io.amaze.bench.api.ActorKey;
import io.amaze.bench.cluster.actor.ActorCreationRequest;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/3/16.
 */
public final class AgentInputMessage implements Serializable {

    private final AgentKey targetAgent;
    private final Action action;
    private final ActorCreationRequest creationRequest;
    private final ActorKey actorToClose;

    private AgentInputMessage(@NotNull final AgentKey targetAgent,
                              @NotNull final Action action,
                              final ActorCreationRequest creationRequest,
                              final ActorKey actorToClose) {

        this.targetAgent = checkNotNull(targetAgent);
        this.action = checkNotNull(action);
        this.creationRequest = creationRequest;
        this.actorToClose = actorToClose;
    }

    public static AgentInputMessage createActor(@NotNull final AgentKey targetAgent,
                                                @NotNull final ActorCreationRequest actorCreationRequest) {
        checkNotNull(targetAgent);
        checkNotNull(actorCreationRequest);
        return new AgentInputMessage(targetAgent, Action.CREATE_ACTOR, actorCreationRequest, null);
    }

    public static AgentInputMessage closeActor(@NotNull final AgentKey targetAgent, @NotNull final ActorKey actorKey) {
        checkNotNull(targetAgent);
        checkNotNull(actorKey);
        return new AgentInputMessage(targetAgent, Action.CLOSE_ACTOR, null, actorKey);
    }

    public Action getAction() {
        return action;
    }

    public AgentKey getTargetAgent() {
        return targetAgent;
    }

    /**
     * @return The creation request to create when action is {@link Action#CREATE_ACTOR}, or {@code null}
     */
    public ActorCreationRequest getCreationRequest() {
        return creationRequest;
    }

    /**
     * @return The actor to close when action is {@link Action#CLOSE_ACTOR}, or {@code null}
     */
    public ActorKey getActorToClose() {
        return actorToClose;
    }

    @Override
    public String toString() {
        return "{\"AgentInputMessage\":{" + //
                "\"targetAgent\":" + targetAgent + ", " + //
                "\"action\":\"" + action + "\"" + ", " + //
                "\"creationRequest\":" + creationRequest + ", " + //
                "\"actorToClose\":" + actorToClose + "}}";
    }

    public enum Action {
        CREATE_ACTOR, //
        CLOSE_ACTOR //
    }
}
