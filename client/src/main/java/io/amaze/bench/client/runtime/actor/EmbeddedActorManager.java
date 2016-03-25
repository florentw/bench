package io.amaze.bench.client.runtime.actor;


import javax.validation.constraints.NotNull;

/**
 * Instantiates actors using the given factory, in the local JVM.
 * <p>
 * Created on 2/28/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class EmbeddedActorManager extends AbstractActorManager {

    public EmbeddedActorManager(@NotNull final ActorFactory factory) {
        super(factory);
    }

    @Override
    public void close() {
        // Nothing to close
    }

    @Override
    public ManagedActor createActor(@NotNull final String name,
                                    @NotNull final String className,
                                    @NotNull final String jsonConfig) throws ValidationException {
        final Actor actor = getFactory().create(name, className, jsonConfig);
        return new ManagedActor() {
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
