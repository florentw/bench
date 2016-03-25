package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.api.actor.Actor;
import io.amaze.bench.client.api.actor.After;
import io.amaze.bench.client.api.actor.Sender;

/**
 * Created on 3/6/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
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
