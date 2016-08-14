package io.amaze.bench.client.runtime.message;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 2/24/16
 *
 * @author Florent Weber (florent.weber@gmail.com)
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
