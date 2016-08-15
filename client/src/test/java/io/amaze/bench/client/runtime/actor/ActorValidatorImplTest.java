package io.amaze.bench.client.runtime.actor;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.client.api.Actor;
import io.amaze.bench.client.api.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static io.amaze.bench.client.runtime.actor.ActorValidatorImpl.*;
import static io.amaze.bench.client.runtime.actor.ActorValidators.get;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created on 3/1/16.
 */
public final class ActorValidatorImplTest {

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
        expectedException.expectMessage(String.format(MSG_IMPLEMENT_REACTOR, Reactor.class.getName()));
        expectedException.expectMessage("An actor class must have annotation @io.amaze.bench.client.api.Actor");

        get().loadAndValidate(EmptyActor.class.getName());
    }

    @Test
    public void abstract_actor_throws() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(MSG_ABSTRACT_CLASS);

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
        expectedException.expectMessage(String.format(MSG_IMPLEMENT_REACTOR, Reactor.class.getName()));

        get().loadAndValidate(NoReactor.class.getName());
    }

    @Test
    public void actor_implementing_sender_throws() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("An actor class must not implement io.amaze.bench.client.api.Sender");

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

    @io.amaze.bench.client.api.Actor
    private static final class NoReactor {
        public NoReactor() {
            // Dummy
        }
    }

    @Actor
    private static final class ImplementSender implements Reactor, Sender {
        public ImplementSender() {
            // Dummy
        }

        @Override
        public void onMessage(@NotNull final String from, @NotNull final Serializable message) {
            // Dummy
        }

        @Override
        public void send(@NotNull final String to, @NotNull final Serializable message) {
            // Dummy
        }
    }

    @Actor
    private static final class TwoBeforeMethods implements Reactor {
        public TwoBeforeMethods() {
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

        @Override
        public void onMessage(@NotNull final String from, @NotNull final Serializable message) {
            // Dummy
        }
    }

    @Actor
    private static final class TwoAfterMethods implements Reactor {
        public TwoAfterMethods() {
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

        @Override
        public void onMessage(@NotNull final String from, @NotNull final Serializable message) {
            // Dummy
        }
    }

    @Actor
    private class PrivateConstructorActor implements Reactor {
        private PrivateConstructorActor() {
        }

        @Override
        public void onMessage(@NotNull final String from, @NotNull final Serializable message) {
            // Dummy
        }
    }

    @Actor
    private abstract class AbstractActor implements Reactor {

    }
}