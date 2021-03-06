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

import io.amaze.bench.api.*;
import io.amaze.bench.cluster.actor.ValidationException;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static io.amaze.bench.shared.util.Reflection.findAtMostOneAnnotatedMethod;
import static java.util.Objects.requireNonNull;


/**
 * In charge of loading the {@link Reactor} classes and validating them, so that they can be handled by the runtime.
 *
 * @see ActorValidators
 * @see Reactor
 */
public final class ActorValidator {

    static final String MSG_PUBLIC_CONSTRUCTOR = "An actor class have at least one public constructor.";
    static final String MSG_ABSTRACT_CLASS = "An actor class must not be abstract";
    static final String MSG_IMPLEMENT_REACTOR = "An actor class must implement %s";
    static final String MSG_MISSING_ANNOTATION = "An actor class must have annotation @%s";

    private static final String VALIDATION_MSG = "Validation failed for class \"%s\".";
    private static final String LOAD_MSG = "Could not load class \"%s\".";

    ActorValidator() {
        // Should not be public
    }

    /**
     * Attempts to load the {@link Reactor} class using the current classpath,<br>
     * a set of checks is then performed on the resulting Class,<br>
     * if one or more checks fail, a {@link ValidationException} is thrown.
     *
     * @param className Fully qualified name of the class implementing {@link Reactor}.
     * @return The loaded class.
     * @throws ValidationException If routine checks failed after loading the class.
     */
    @NotNull
    public Class<? extends Reactor> loadAndValidate(@NotNull final String className) throws ValidationException {
        requireNonNull(className);

        Class<?> clazz = loadClass(className);

        Verify verify = new Verify(clazz);
        verify.mustImplementReactor();
        verify.mustHaveAPublicConstructor();
        verify.mustNotBeAbstract();
        verify.mustDeclareActor();
        verify.mustNotImplementSender();
        verify.canDeclareOneBeforeMethod();
        verify.canDeclareOneAfterMethod();
        verify.canDeclareOneBootstrapMethod();

        if (!verify.causes().isEmpty()) {
            throw ValidationException.create(String.format(VALIDATION_MSG, className), verify.causes());
        }

        return verify.resultingClass();
    }

    private static Class<?> loadClass(@NotNull final String className) throws ValidationException {
        try {
            return Class.forName(className); // NOSONAR
        } catch (ClassNotFoundException e) {
            throw ValidationException.create(String.format(LOAD_MSG, className), e);
        }
    }

    private static final class Verify {

        private final List<Exception> causes = new ArrayList<>();
        private final Class<?> inputActorClass;

        private Class<? extends Reactor> typedOutputClass;

        Verify(final Class<?> inputActorClass) {
            this.inputActorClass = inputActorClass;
        }

        @NotNull
        List<Exception> causes() {
            return new ArrayList<>(causes);
        }

        Class<? extends Reactor> resultingClass() {
            return typedOutputClass;
        }

        void mustHaveAPublicConstructor() {
            if (inputActorClass.getConstructors().length == 0) {
                causes.add(new IllegalArgumentException(MSG_PUBLIC_CONSTRUCTOR));
            }
        }

        void mustNotBeAbstract() {
            if (Modifier.isAbstract(inputActorClass.getModifiers())) {
                causes.add(new IllegalArgumentException(MSG_ABSTRACT_CLASS));
            }
        }

        void mustDeclareActor() {
            if (inputActorClass.getAnnotation(io.amaze.bench.api.Actor.class) == null) {
                causes.add(new IllegalArgumentException(String.format(MSG_MISSING_ANNOTATION,
                                                                      io.amaze.bench.api.Actor.class.getName())));
            }
        }

        void canDeclareOneAfterMethod() {
            try {
                findAtMostOneAnnotatedMethod(inputActorClass, After.class);
            } catch (IllegalArgumentException e) {
                causes.add(e);
            }
        }

        void canDeclareOneBeforeMethod() {
            try {
                findAtMostOneAnnotatedMethod(inputActorClass, Before.class);
            } catch (IllegalArgumentException e) {
                causes.add(e);
            }
        }

        void canDeclareOneBootstrapMethod() {
            try {
                findAtMostOneAnnotatedMethod(inputActorClass, Bootstrap.class);
            } catch (IllegalArgumentException e) {
                causes.add(e);
            }
        }

        void mustNotImplementSender() {
            boolean failed = true;
            try {
                inputActorClass.asSubclass(Sender.class);
            } catch (ClassCastException e) { // NOSONAR
                failed = false;
            }
            if (failed) {
                causes.add(new IllegalArgumentException("An actor class must not implement " + Sender.class.getName()));
            }
        }

        void mustImplementReactor() {
            try {
                this.typedOutputClass = inputActorClass.asSubclass(Reactor.class);
            } catch (ClassCastException e) { // NOSONAR
                causes.add(new IllegalArgumentException(String.format(MSG_IMPLEMENT_REACTOR, Reactor.class.getName())));
            }
        }
    }
}
