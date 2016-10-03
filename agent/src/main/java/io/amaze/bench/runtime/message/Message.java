/*
 * Copyright 2016 Florent Weber <florent.weber@gmail.com>
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
package io.amaze.bench.runtime.message;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 2/24/16
 */
public final class Message<T extends Serializable> implements Serializable {

    private final String from;
    private final T data;

    public Message(@NotNull final String from, @NotNull final T data) {
        this.from = checkNotNull(from);
        this.data = checkNotNull(data);
    }

    @NotNull
    public final String from() {
        return from;
    }

    @NotNull
    public final T data() {
        return data;
    }

    @Override
    public String toString() {
        return "{\"Message\":{" + //
                "\"from\":\"" + from + "\"" + ", " + //
                "\"data\":\"" + data + "\"}}";
    }
}
