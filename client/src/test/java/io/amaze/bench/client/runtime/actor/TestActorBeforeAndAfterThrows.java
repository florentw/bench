package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.api.actor.After;
import io.amaze.bench.client.api.actor.Before;
import io.amaze.bench.client.api.actor.Sender;

/**
 * Created on 3/13/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class TestActorBeforeAndAfterThrows extends TestActor {
    public TestActorBeforeAndAfterThrows(final Sender sender) {
        super(sender);
    }

    @Before
    @Override
    public void before() {
        throw new IllegalArgumentException();
    }

    @After
    @Override
    public void after() {
        throw new IllegalArgumentException();
    }
}
