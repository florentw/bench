package io.amaze.bench.orchestrator;

import javax.validation.constraints.NotNull;

/**
 * Created on 3/9/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class ActorConfig {

    private final String name;
    private final String className;

    private final DeployConfig deploy;

    public ActorConfig(@NotNull final String name,
                       @NotNull final String className,
                       @NotNull final DeployConfig deploy) {
        this.name = name;
        this.className = className;
        this.deploy = deploy;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public DeployConfig getDeploy() {
        return deploy;
    }

    public static class DeployConfig {
        private final boolean forked;
        private final String hostGroup;

        public DeployConfig(final boolean forked, final String hostGroup) {
            this.forked = forked;
            this.hostGroup = hostGroup;
        }

        public boolean isForked() {
            return forked;
        }

        public String getHostGroup() {
            return hostGroup;
        }
    }
}
