/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.amaze.bench.runtime.actor;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.api.*;
import io.amaze.bench.cluster.actor.ValidationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static io.amaze.bench.runtime.actor.ActorValidator.*;
import static io.amaze.bench.runtime.actor.ActorValidators.get;
import static java.lang.String.format;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created on 3/1/16.
 */
public final class ActorValidatorTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(ActorValidator.class);
        tester.testAllPublicInstanceMethods(new ActorValidator());
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
        expectedException.expectMessage(format(MSG_IMPLEMENT_REACTOR, Reactor.class.getName()));
        expectedException.expectMessage(format(MSG_MISSING_ANNOTATION, Actor.class.getName()));

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
        expectedException.expectMessage(format(MSG_IMPLEMENT_REACTOR, Reactor.class.getName()));

        get().loadAndValidate(NoReactor.class.getName());
    }

    @Test
    public void actor_implementing_sender_throws() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(format("An actor class must not implement %s", Sender.class.getName()));

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

    @Actor
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