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

package io.amaze.bench.scenario;

import io.amaze.bench.runtime.actor.ActorConfig;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 4/17/16.
 */
public final class Scenario {

    private final String name;
    private final List<ActorConfig> actors;

    public Scenario(final String name, final List<ActorConfig> actors) {
        this.name = checkNotNull(name);
        this.actors = checkNotNull(actors);
    }

    public String getName() {
        return name;
    }

    public List<ActorConfig> getActors() {
        return actors;
    }
}
