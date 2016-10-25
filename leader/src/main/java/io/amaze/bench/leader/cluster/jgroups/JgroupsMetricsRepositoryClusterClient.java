package io.amaze.bench.leader.cluster.jgroups;

import io.amaze.bench.leader.cluster.registry.MetricsRepositoryClusterClient;
import io.amaze.bench.leader.cluster.registry.MetricsRepositoryListener;
import io.amaze.bench.runtime.actor.metric.MetricValuesMessage;
import io.amaze.bench.shared.jgroups.JgroupsListener;
import io.amaze.bench.shared.jgroups.JgroupsListenerMultiplexer;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/23/16.
 */
public final class JgroupsMetricsRepositoryClusterClient implements MetricsRepositoryClusterClient {

    private final JgroupsListenerMultiplexer multiplexer;
    private JgroupsListener<MetricValuesMessage> jgroupsListener;

    public JgroupsMetricsRepositoryClusterClient(@NotNull final JgroupsListenerMultiplexer multiplexer) {
        this.multiplexer = checkNotNull(multiplexer);
    }

    @Override
    public void startMetricsListener(@NotNull final MetricsRepositoryListener metricsListener) {
        checkNotNull(metricsListener);

        jgroupsListener = (msg, payload) -> {
            checkNotNull(msg);
            checkNotNull(payload);

            metricsListener.onMetricValues(payload);
        };
        multiplexer.addListener(MetricValuesMessage.class, jgroupsListener);
    }

    @Override
    public void close() {
        multiplexer.removeListener(jgroupsListener);
    }

}
