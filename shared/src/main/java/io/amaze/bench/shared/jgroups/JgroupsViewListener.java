package io.amaze.bench.shared.jgroups;

import org.jgroups.Address;

import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * Created on 10/8/16.
 */
public interface JgroupsViewListener {

    void initialView(Collection<Address> members);

    void memberJoined(@NotNull Address address);

    void memberLeft(@NotNull Address address);

}
