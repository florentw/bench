package io.amaze.bench.client.runtime.actor;


import org.jetbrains.annotations.NotNull;

/**
 * Created on 2/29/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface ActorManager extends AutoCloseable {

    @NotNull
    ManagedActor createActor(@NotNull ActorConfig actorConfig) throws ValidationException;

    @Override
    void close();

}
