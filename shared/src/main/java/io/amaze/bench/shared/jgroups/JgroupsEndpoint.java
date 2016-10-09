package io.amaze.bench.shared.jgroups;

import com.google.common.base.Throwables;
import io.amaze.bench.Endpoint;
import org.jgroups.Address;
import org.jgroups.util.Util;

import javax.validation.constraints.NotNull;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/8/16.
 */
public final class JgroupsEndpoint implements Endpoint {

    private final byte[] serializedEndpoint;
    private final Class<? extends Address> addressClass;

    public JgroupsEndpoint(@NotNull final Address address) {
        checkNotNull(address);
        try {
            this.addressClass = address.getClass();
            serializedEndpoint = Util.streamableToByteBuffer(address);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public Address getAddress() {
        try {
            return Util.streamableFromByteBuffer(addressClass, serializedEndpoint);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getAddress());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JgroupsEndpoint that = (JgroupsEndpoint) o;
        return that.getAddress().compareTo(getAddress()) == 0;
    }

    @Override
    public String toString() {
        return "{\"JgroupsEndpoint\":\"" + getAddress() + "\"}";
    }
}
