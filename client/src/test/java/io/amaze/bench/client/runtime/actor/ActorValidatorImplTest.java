package io.amaze.bench.client.runtime.actor;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.client.api.ReactorException;
import io.amaze.bench.client.api.actor.After;
import io.amaze.bench.client.api.actor.Before;
import io.amaze.bench.client.api.actor.Reactor;
import io.amaze.bench.client.api.actor.Sender;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.Serializable;

import static io.amaze.bench.client.runtime.actor.ActorValidators.get;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created on 3/1/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class ActorValidatorImplTest {

    private static final String MSG_PUBLIC_CONSTRUCTOR = "An actor class have at least one public constructor.";
    private static final String MSG_IMPLEMENT_REACTOR = "An actor class must implement io.amaze.bench.client.api.actor.Reactor";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(ActorValidatorImpl.class);
        tester.testAllPublicInstanceMethods(new ActorValidatorImpl());
    }

    @Test
    public void invalid_actor_class_throws() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Could not load class");

        get().loadAndValidate("DUMMY");
    }

    @Test
    public void empty_actor_class_throws() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(MSG_PUBLIC_CONSTRUCTOR);
        expectedException.expectMessage(MSG_IMPLEMENT_REACTOR);
        expectedException.expectMessage("An actor class must have annotation @io.amaze.bench.client.api.actor.Actor");

        get().loadAndValidate(EmptyActor.class.getName());
    }

    @Test
    public void abstract_actor_throws() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("An actor class must not be abstract");

        get().loadAndValidate(AbstractActor.class.getName());
    }

    @Test
    public void no_public_constructors_throws() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(MSG_PUBLIC_CONSTRUCTOR);

        get().loadAndValidate(PrivateConstructorActor.class.getName());
    }

    @Test
    public void actor_not_implementing_reactor_throws() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(MSG_IMPLEMENT_REACTOR);

        get().loadAndValidate(NoReactor.class.getName());
    }

    @Test
    public void actor_implementing_sender_throws() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("An actor class must not implement io.amaze.bench.client.api.actor.Sender");

        get().loadAndValidate(ImplementSender.class.getName());
    }

    @Test
    public void actor_declaring_two_before_methods_throws() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("@Before annotation is declared for more than one method");

        get().loadAndValidate(TwoBeforeMethods.class.getName());
    }

    @Test//(expected = ValidationException.class)
    public void actor_declaring_two_after_methods_throws() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("@After annotation is declared for more than one method");

        get().loadAndValidate(TwoAfterMethods.class.getName());
    }

    @Test
    public void valid_actor_returns_typed_actor() throws ValidationException {
        Class<? extends Reactor> typed = get().loadAndValidate(TestActor.class.getName());
        assertNotNull(typed);
    }

    private static final class EmptyActor {

    }

    @io.amaze.bench.client.api.actor.Actor
    private static final class NoReactor {
        public NoReactor() {
            // Dummy
        }
    }

    @io.amaze.bench.client.api.actor.Actor
    private static final class ImplementSender implements Reactor, Sender {
        public ImplementSender() {
            // Dummy
        }

        @Override
        public void onMessage(final String from, final Serializable message) throws ReactorException {
            // Dummy
        }

        @Override
        public void send(final String to, final Serializable message) {
            // Dummy
        }
    }

    @io.amaze.bench.client.api.actor.Actor
    private static final class TwoBeforeMethods implements Reactor {
        public TwoBeforeMethods() {
            // Dummy
        }

        @Override
        public void onMessage(final String from, final Serializable message) throws ReactorException {
            // Dummy
        }

        @Before
        public void before1() {
            // Dummy
        }

        @Before
        public void before2() {
            // Dummy
        }
    }

    @io.amaze.bench.client.api.actor.Actor
    private static final class TwoAfterMethods implements Reactor {
        public TwoAfterMethods() {
            // Dummy
        }

        @Override
        public void onMessage(final String from, final Serializable message) throws ReactorException {
            // Dummy
        }

        @After
        public void after1() {
            // Dummy
        }

        @After
        public void after2() {
            // Dummy
        }
    }

    @io.amaze.bench.client.api.actor.Actor
    private class PrivateConstructorActor implements Reactor {
        private PrivateConstructorActor() {
        }

        @Override
        public void onMessage(final String from, final Serializable message) throws ReactorException {
            // Dummy
        }
    }

    @io.amaze.bench.client.api.actor.Actor
    private abstract class AbstractActor implements Reactor {

    }
}