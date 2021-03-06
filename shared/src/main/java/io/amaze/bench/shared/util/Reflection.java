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
package io.amaze.bench.shared.util;


import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Created on 2/28/16.
 */
public final class Reflection {

    private static final String MSG_DECLARED_MORE_THAN_ONCE = "%s annotation is declared for more than one method of class %s";

    private Reflection() {
        // Helper class
    }

    /**
     * Helper to find a method annotated with the given annotation. If no method is annotated, the optional is empty.<br>
     * If more than one method in the input class in annotated, a {@link java.lang.IllegalArgumentException} is thrown.
     *
     * @param clazz      An input class to look for annotated methods in
     * @param annotation The method annotation to look for
     * @return An annotated method if found
     * @throws IllegalArgumentException If more than one method is annotated with the given annotation
     */
    public static Optional<Method> findAtMostOneAnnotatedMethod(@NotNull final Class<?> clazz,
                                                                @NotNull final Class<? extends Annotation> annotation) {
        Method found = null;
        for (Method m : clazz.getMethods()) {
            Annotation an = m.getAnnotation(annotation);
            if (an == null) {
                continue;
            }

            if (found != null) {
                String msg = String.format(MSG_DECLARED_MORE_THAN_ONCE,
                                           "@" + annotation.getSimpleName(),
                                           clazz.getCanonicalName());

                throw new IllegalArgumentException(msg);
            } else {
                found = m;
            }
        }
        return Optional.ofNullable(found);
    }

}
