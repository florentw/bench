package io.amaze.bench.orchestrator;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/9/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface ResourceManager extends AutoCloseable {

    /**
     * Requests an actor instantiation using the given {@link ActorConfig} configuration.
     *
     * @param actorConfig The actor configuration.
     * @throws IllegalStateException if the agent cannot be created before
     */
    void createActor(@NotNull ActorConfig actorConfig);

    /**
     * Requests to close an actor. It will contact the agent hosting the actor in order for it to close it.
     *
     * @param name The name of the actor to close.
     * @throws IllegalArgumentException if the actor does not exist.
     */
    void closeActor(@NotNull String name);

    /**
     * Closes every actor handled by this ResourceManager
     */
    @Override
    void close();

}
