package io.amaze.bench.runtime.cluster;

import com.typesafe.config.Config;
import io.amaze.bench.runtime.actor.ActorKey;

import javax.validation.constraints.NotNull;
import java.io.Closeable;

/**
 * Used to generate a specific cluster configuration for a new forked actor.
 * It could be used to generate a different port for each.
 */
public interface ClusterConfigFactory extends Closeable {

    /**
     * @param actorKey
     * @return
     */
    @NotNull
    Config clusterConfigFor(@NotNull ActorKey actorKey);

    @Override
    void close();
}
