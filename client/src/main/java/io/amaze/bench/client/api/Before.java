/*
 * Copyright 2016 Florent Weber <florent.weber@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.amaze.bench.client.api;

import java.lang.annotation.*;

/**
 * A class annotated with @{@link Actor} can use @{@link Before} to annotate one of its methods.<br/>
 * This method will be called as a post-initialization hook.<br/>
 * At most one method can be tagged with @{@link Before}.
 * <p/>
 * Created on 2/28/16.
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Before {

}
