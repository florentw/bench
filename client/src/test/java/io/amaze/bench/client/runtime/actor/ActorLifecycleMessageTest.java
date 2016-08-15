package io.amaze.bench.client.runtime.actor;

import com.google.common.base.Predicate;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created on 8/15/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ActorLifecycleMessageTest {

    private ActorLifecycleMessage msg;

    @Before
    public void init() {
        msg = new ActorLifecycleMessage("", "", ActorLifecycleMessage.Phase.INITIALIZED);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        List<? extends Constructor<?>> constructors = Arrays.asList(ActorLifecycleMessage.class.getConstructors());
        Constructor<?> constructor = from(constructors).filter(new Predicate<Constructor<?>>() {
            @Override
            public boolean apply(@Nullable final Constructor<?> input) {
                return input != null && input.getParameterTypes().length == 3;
            }
        }).first().orNull();

        tester.testConstructor(constructor);
        tester.testAllPublicInstanceMethods(msg);
    }

    @Test
    public void serialize_deserialize() {
        ActorLifecycleMessage received = SerializableTester.reserialize(msg);

        assertThat(received.getActor(), is(msg.getActor()));
        assertThat(received.getAgent(), is(msg.getAgent()));
        assertThat(received.getPhase(), is(msg.getPhase()));
        assertThat(received.getThrowable(), is(msg.getThrowable()));
    }

}