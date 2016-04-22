package io.amaze.bench.client.runtime.actor;

import org.jetbrains.annotations.NotNull;

/**
 * Created on 3/13/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface ManagedActor {

    @NotNull
    String name();

    void close();

}
