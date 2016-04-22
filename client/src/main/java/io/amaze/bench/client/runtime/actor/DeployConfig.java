package io.amaze.bench.client.runtime.actor;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 4/6/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class DeployConfig implements Serializable {

    private final String jmsServerHost;
    private final int jmsServerPort;
    private final boolean forked;
    private final List<String> preferredHosts;

    public DeployConfig(@NotNull final String jmsServerHost,
                        @NotNull final int jmsServerPort,
                        final boolean forked,
                        @NotNull final List<String> preferredHosts) {

        this.jmsServerHost = checkNotNull(jmsServerHost);
        this.jmsServerPort = checkNotNull(jmsServerPort);
        this.forked = forked;
        this.preferredHosts = checkNotNull(preferredHosts);
    }

    @NotNull
    public List<String> getPreferredHosts() {
        return Collections.unmodifiableList(preferredHosts);
    }

    public boolean isForked() {
        return forked;
    }

    String getJmsServerHost() {
        return jmsServerHost;
    }

    int getJmsServerPort() {
        return jmsServerPort;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeployConfig that = (DeployConfig) o;
        return jmsServerPort == that.jmsServerPort &&
                forked == that.forked &&
                Objects.equals(jmsServerHost, that.jmsServerHost) &&
                Objects.equals(preferredHosts, that.preferredHosts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jmsServerHost, jmsServerPort, forked, preferredHosts);
    }

    @Override
    public String toString() {
        return "DeployConfig{" +
                "jmsServerHost='" + jmsServerHost + '\'' +
                ", jmsServerPort=" + jmsServerPort +
                ", forked=" + forked +
                ", preferredHosts=" + preferredHosts +
                '}';
    }

}
