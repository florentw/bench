package io.amaze.bench.runtime.cluster.registry;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.actor.ActorDeployInfo;
import io.amaze.bench.runtime.actor.ActorKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created on 10/14/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class RegisteredActorTest {

    private static final ActorKey ACTOR_KEY = new ActorKey("dummy");
    @Mock
    private Endpoint endpoint;

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorKey.class, ACTOR_KEY);
        tester.setDefault(ActorDeployInfo.class, new ActorDeployInfo(endpoint, 10));
        tester.setDefault(RegisteredActor.class, registeredActor(ACTOR_KEY));

        tester.testAllPublicConstructors(RegisteredActor.class);
        tester.testAllPublicStaticMethods(RegisteredActor.class);
        tester.testAllPublicInstanceMethods(registeredActor(ACTOR_KEY));
    }

    @Test
    public void serializable() {
        RegisteredActor expected = registeredActor(ACTOR_KEY);
        RegisteredActor actual = SerializableTester.reserialize(expected);

        assertThat(expected.getAgentHost(), is(actual.getAgentHost()));
        assertThat(expected.getDeployInfo(), is(actual.getDeployInfo()));
        assertThat(expected.getKey(), is(actual.getKey()));
        assertThat(expected.getState(), is(actual.getState()));
    }

    private RegisteredActor registeredActor(final ActorKey actorKey) {
        return RegisteredActor.created(actorKey, "dummy");
    }

    public static class DummyEndpoint implements Endpoint, Serializable {

        private final String key;

        public DummyEndpoint(final String key) {
            this.key = key;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DummyEndpoint that = (DummyEndpoint) o;
            return Objects.equals(key, that.key);
        }
    }

}