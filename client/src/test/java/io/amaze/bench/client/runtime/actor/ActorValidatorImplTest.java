package io.amaze.bench.client.runtime.actor;

import io.amaze.bench.client.api.ReactorException;
import io.amaze.bench.client.api.actor.After;
import io.amaze.bench.client.api.actor.Before;
import io.amaze.bench.client.api.actor.Reactor;
import io.amaze.bench.client.api.actor.Sender;
import org.junit.Test;

import java.io.Serializable;

import static io.amaze.bench.client.runtime.actor.ActorValidators.get;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created on 3/1/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class ActorValidatorImplTest {

    @Test(expected = ValidationException.class)
    public void invalid_actor_class_throws() throws ValidationException {
        get().loadAndValidate("DUMMY");
    }

    @Test(expected = ValidationException.class)
    public void empty_actor_class_throws() throws ValidationException {
        get().loadAndValidate(EmptyActor.class.getName());
    }

    @Test(expected = ValidationException.class)
    public void abstract_actor_throws() throws ValidationException {
        get().loadAndValidate(AbstractActor.class.getName());
    }

    @Test(expected = ValidationException.class)
    public void no_public_constructors_throws() throws ValidationException {
        get().loadAndValidate(PrivateConstructorActor.class.getName());
    }

    @Test(expected = ValidationException.class)
    public void actor_not_implementing_reactor_throws() throws ValidationException {
        get().loadAndValidate(NoReactor.class.getName());
    }

    @Test(expected = ValidationException.class)
    public void actor_implementing_sender_throws() throws ValidationException {
        get().loadAndValidate(ImplementSender.class.getName());
    }

    @Test(expected = ValidationException.class)
    public void actor_declaring_two_before_methods_throws() throws ValidationException {
        get().loadAndValidate(TwoBeforeMethods.class.getName());
    }

    @Test(expected = ValidationException.class)
    public void actor_declaring_two_after_methods_throws() throws ValidationException {
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

    }

    @io.amaze.bench.client.api.actor.Actor
    private static final class ImplementSender implements Reactor, Sender {
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