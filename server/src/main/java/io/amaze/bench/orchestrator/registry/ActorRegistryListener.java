package io.amaze.bench.orchestrator.registry;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/28/16.
 */
public interface ActorRegistryListener {

    void onActorCreated(@NotNull String name, @NotNull String agent);

    void onActorInitialized(@NotNull String name, @NotNull String agent);

    void onActorFailed(@NotNull String name, @NotNull Throwable throwable);

    void onActorClosed(@NotNull String name);

}
