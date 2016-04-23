package io.amaze.bench.client.runtime.actor;


import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Instantiates actors using the given factory, in the local JVM.
 * <p>
 * Created on 2/28/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class EmbeddedActorManager extends AbstractActorManager {

    private final ActorFactory factory;

    public EmbeddedActorManager(@NotNull final String agent, @NotNull final ActorFactory factory) {
        super(agent);
        this.factory = checkNotNull(factory);
    }

    @Override
    public void close() {
        // Nothing to close
    }

    @NotNull
    @Override
    public ManagedActor createActor(@NotNull final ActorConfig actorConfig) throws ValidationException {
        checkNotNull(actorConfig);

        final String name = actorConfig.getName();
        final Actor actor = factory.create(name, actorConfig.getClassName(), actorConfig.getActorJsonConfig());

        return new ManagedActor() {
            @NotNull
            @Override
            public String name() {
                return name;
            }

            @Override
            public void close() {
                actor.close();
            }
        };
    }
}
