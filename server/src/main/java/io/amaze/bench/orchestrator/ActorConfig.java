package io.amaze.bench.orchestrator;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * Created on 3/9/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class ActorConfig {

    private final String name;
    private final String className;

    private final DeployConfig deployConfig;

    public ActorConfig(@NotNull final String name,
                       @NotNull final String className, @NotNull final DeployConfig deployConfig) {
        this.name = name;
        this.className = className;
        this.deployConfig = deployConfig;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public DeployConfig getDeployConfig() {
        return deployConfig;
    }

    public static final class DeployConfig {
        private final boolean forked;
        private final List<String> preferredHosts;

        public DeployConfig(final boolean forked, final List<String> preferredHosts) {
            this.forked = forked;
            this.preferredHosts = preferredHosts;
        }

        public List<String> getPreferredHosts() {
            return Collections.unmodifiableList(preferredHosts);
        }

        public String getAgentJsonConfig() {
            return "\"forked\":\"" + forked + "\"";
        }
    }
}
