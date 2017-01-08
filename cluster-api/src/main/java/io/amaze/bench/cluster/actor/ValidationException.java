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
package io.amaze.bench.cluster.actor;

import com.google.common.base.Throwables;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 3/1/16.
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
