package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.api.Actor;
import io.amaze.bench.client.api.IrrecoverableException;
import io.amaze.bench.client.api.Reactor;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/13/16.
 */
@Actor
public class TestActorNoBeforeNoAfter implements Reactor<String> {
    public TestActorNoBeforeNoAfter() {
        super();
    }

    @Override
    public void onMessage(@NotNull final String from, @NotNull final String message) throws IrrecoverableException {
        // Dummy
    }
}
