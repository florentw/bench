package io.amaze.bench.client.runtime.actor;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.shared.test.Json;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_ACTOR;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created on 8/15/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ActorLifecycleMessageTest {

    private static final ActorDeployInfo ACTOR_DEPLOY_INFO = new ActorDeployInfo(10);
    private static final ActorKey ACTOR_KEY = new ActorKey("actor");

    private ActorLifecycleMessage msg;

    @Before
    public void init() {
        msg = ActorLifecycleMessage.initialized(ACTOR_KEY, ACTOR_DEPLOY_INFO);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorDeployInfo.class, ACTOR_DEPLOY_INFO);
        tester.setDefault(ActorKey.class, DUMMY_ACTOR);

        tester.testAllPublicStaticMethods(ActorLifecycleMessage.class);
        tester.testAllPublicInstanceMethods(msg);
    }

    @Test
    public void serialize_deserialize() {
        ActorLifecycleMessage received = SerializableTester.reserialize(msg);

        assertThat(received.getActor(), is(msg.getActor()));
        assertThat(received.getAgent(), is(msg.getAgent()));
        assertThat(received.getState(), is(msg.getState()));
        assertThat(received.getDeployInfo(), is(msg.getDeployInfo()));
        assertThat(received.getThrowable(), is(msg.getThrowable()));
    }

    @Test
    public void toString_should_yield_valid_json() {
        assertTrue(Json.isValid(msg.toString()));
    }

}