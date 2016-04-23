package io.amaze.bench.client.runtime.actor;


import javax.validation.constraints.NotNull;

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
