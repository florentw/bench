package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.api.Actor;
import io.amaze.bench.client.api.Before;
import io.amaze.bench.client.api.Sender;

/**
 * Created on 3/6/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
@Actor
public final class TestActorBeforeThrows extends TestActor {
    public TestActorBeforeThrows(final Sender sender) {
        super(sender);
    }

    @Before
    @Override
    public void before() {
        throw new IllegalArgumentException();
    }
}