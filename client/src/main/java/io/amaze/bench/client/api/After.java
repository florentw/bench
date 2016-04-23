package io.amaze.bench.client.api;

import java.lang.annotation.*;

/**
 * A class annotated with @{@link Actor} can use @{@link After} to annotate one of its methods.<br/>
 * This method will be called when the Actor is destroyed.<br/>
 * At most one method can be tagged with @{@link After}.
 * <p/>
 * Created on 2/28/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface After {

}
