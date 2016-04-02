package io.amaze.bench.orchestrator;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/9/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface ActorManagerProxy {

    void initActor(@NotNull ActorConfig actorConfig);

    void startActor(@NotNull String name);

    void closeActor(@NotNull String name);

}
