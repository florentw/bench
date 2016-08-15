package io.amaze.bench.client.runtime.actor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/9/16.
 */
public final class ActorConfig implements Serializable {

    private final String name;
    private final String className;
    private final DeployConfig deployConfig;
    private final String actorJsonConfig;

    public ActorConfig(@NotNull final String name,
                       @NotNull final String className,
                       @NotNull final DeployConfig deployConfig,
                       @NotNull final String actorJsonConfig) {

        this.name = checkNotNull(name);
        this.className = checkNotNull(className);
        this.deployConfig = checkNotNull(deployConfig);
        this.actorJsonConfig = checkNotNull(actorJsonConfig);
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getClassName() {
        return className;
    }

    @NotNull
    public DeployConfig getDeployConfig() {
        return deployConfig;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, className, deployConfig, actorJsonConfig);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActorConfig that = (ActorConfig) o;
        return Objects.equals(name, that.name) && Objects.equals(className, that.className) && Objects.equals(
                deployConfig,
                that.deployConfig) && Objects.equals(actorJsonConfig, that.actorJsonConfig);
    }

    @Override
    public String toString() {
        return "{\"ActorConfig\":{" + //
                "\"name\":\"" + name + "\"" + ", " + //
                "\"className\":\"" + className + "\"" + ", " + //
                "\"deployConfig\":" + deployConfig + ", " + //
                "\"actorJsonConfig\":" + actorJsonConfig + "" + "}}";
    }

    @NotNull
    String getActorJsonConfig() {
        return actorJsonConfig;
    }
}
