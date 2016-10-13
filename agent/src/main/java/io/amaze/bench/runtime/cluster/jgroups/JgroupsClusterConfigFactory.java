package io.amaze.bench.runtime.cluster.jgroups;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.cluster.ClusterConfigFactory;

import javax.validation.constraints.NotNull;

/**
 * Created on 10/11/16.
 */
public final class JgroupsClusterConfigFactory implements ClusterConfigFactory {

    @Override
    public Config clusterConfigFor(@NotNull final ActorKey actorKey) {
        return ConfigFactory.empty();
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
