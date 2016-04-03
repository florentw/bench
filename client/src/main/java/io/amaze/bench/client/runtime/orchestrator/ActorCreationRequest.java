package io.amaze.bench.client.runtime.orchestrator;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Message sent by the master to an agent when it is required to createForAgent an actor.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class ActorCreationRequest implements Serializable {

    private final String actor;
    private final String className;
    private final String jsonConfig;

    public ActorCreationRequest(@NotNull final String actor,
                                @NotNull final String className,
                                @NotNull final String jsonConfig) {
        this.actor = actor;
        this.className = className;
        this.jsonConfig = jsonConfig;
    }

    public String getActor() {
        return actor;
    }

    public String getClassName() {
        return className;
    }

    String getJsonConfig() {
        return jsonConfig;
    }
}
