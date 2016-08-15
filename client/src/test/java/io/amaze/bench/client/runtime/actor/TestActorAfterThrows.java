package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.api.Actor;
import io.amaze.bench.client.api.After;
import io.amaze.bench.client.api.Sender;

/**
 * Created on 3/6/16.
 */
@Actor
public class TestActorAfterThrows extends TestActor {
    public TestActorAfterThrows(final Sender sender) {
        super(sender);
    }

    @After
    @Override
    public void after() {
        throw new IllegalArgumentException();
    }
}
