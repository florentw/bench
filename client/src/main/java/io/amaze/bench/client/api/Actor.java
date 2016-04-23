package io.amaze.bench.client.api;

import java.lang.annotation.*;

/**
 * Annotation for actor implementation classes.<br/>
 * <ul>
 * <li>An class annotated with @Actor must implement the {@link Reactor} interface.<br/></li>
 * <li>The {@link Reactor} interface must be parametrized with the type of messages it will take as input.<br/></li>
 * <li>An actor can declare one of its method as a setup routine, by it annotating with @{@link Before}</li>
 * <li>An actor can declare one of its method as a teardown routine, by it annotating with @{@link After}</li>
 * </ul>
 * <p/>
 * Created on 2/28/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 * @see Reactor
 * @see Sender
 */
@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Actor {

}
