package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.api.ReactorException;
import io.amaze.bench.client.api.actor.Actor;
import io.amaze.bench.client.api.actor.Reactor;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/13/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
@Actor
public class TestActorNoBeforeNoAfter implements Reactor<String> {
    public TestActorNoBeforeNoAfter() {
        super();
    }

    @Override
    public void onMessage(@NotNull final String from, @NotNull final String message) throws ReactorException {
        // Dummy
    }
}
