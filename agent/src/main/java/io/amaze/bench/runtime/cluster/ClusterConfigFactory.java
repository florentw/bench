package io.amaze.bench.runtime.cluster;

import com.typesafe.config.Config;
import io.amaze.bench.runtime.actor.ActorKey;

import javax.validation.constraints.NotNull;
import java.io.Closeable;

/**
 * Created on 10/10/16.
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
