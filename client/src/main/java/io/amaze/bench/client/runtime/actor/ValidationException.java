package io.amaze.bench.client.runtime.actor;

import com.google.common.base.Throwables;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 3/1/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class ValidationException extends Exception {

    private ValidationException(final String message) {
        super(message);
    }

    public static ValidationException create(final String message, final List<Exception> causes) {
        return new ValidationException(appendCauses(message, causes));
    }

    public static ValidationException create(final String message, final Exception cause) {
        List<Exception> causes = new ArrayList<>(1);
        causes.add(cause);
        return new ValidationException(appendCauses(message, causes));
    }

    private static String appendCauses(String msg, List<Exception> causes) {
        StringBuilder out = new StringBuilder(msg);

        for (Exception cause : causes) {
            if (!(cause instanceof IllegalArgumentException)) {
                out.append("\n").append(Throwables.getStackTraceAsString(cause));
            } else {
                out.append("\n* ").append(cause.getClass().getSimpleName()).append(" ").append(cause.getMessage());
            }
        }

        return out.toString();
    }
}
