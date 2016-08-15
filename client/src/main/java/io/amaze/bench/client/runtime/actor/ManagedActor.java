package io.amaze.bench.client.runtime.actor;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/13/16.
 */
public interface ManagedActor {

    @NotNull
    String name();

    void close();

}
