package io.amaze.bench.shared.helper;


import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created on 2/28/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class ReflectionHelper {

    private static final String MSG_DECLARED_MORE_THAN_ONCE = "%s annotation is declared for more than one method of class %s";

    private ReflectionHelper() {
        // Helper class
    }

    /**
     * Helper to find a method annotated with the given annotation. If not method is annotated, null is returned.<br/>
     * If more than one method in the input class in annotated, a {@link java.lang.IllegalArgumentException} is thrown.
     *
     * @param clazz      An input class to look for annotated methods in
     * @param annotation The method annotation to look for
     * @return An annotated method or null if not found
     * @throws IllegalArgumentException If more than one method is annotated with the given annotation
     */
    public static Method findAtMostOneAnnotatedMethod(@NotNull final Class<?> clazz,
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
        return found;
    }

}
