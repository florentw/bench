package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.api.After;
import io.amaze.bench.client.api.Before;
import io.amaze.bench.client.api.Sender;

/**
 * Created on 3/13/16.
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
