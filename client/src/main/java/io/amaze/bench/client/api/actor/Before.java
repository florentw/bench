package io.amaze.bench.client.api.actor;

import java.lang.annotation.*;

/**
 * A class annotated with @{@link Actor} can use @{@link Before} to annotate one of its methods.<br/>
 * This method will be called as a post-initialization hook.<br/>
 * At most one method can be tagged with @{@link Before}.
 * <p/>
 * Created on 2/28/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Before {
}
