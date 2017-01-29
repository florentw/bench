/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
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

package io.amaze.bench.runtime.actor;

import com.typesafe.config.Config;
import io.amaze.bench.api.*;

/**
 * Created on 1/29/17.
 */
@Actor
public final class TestActorBootstrapThrows extends TestActor {

    private boolean afterMethodThrows = false;

    public TestActorBootstrapThrows(final ActorKey actorKey, final Sender sender, final Config config) {
        super(actorKey, sender, config);
    }

    @Bootstrap
    public void bootstrap() {
        throw new IllegalStateException("Provoked exception.");
    }

    @After
    @Override
    public void after() {
        super.after();

        if (afterMethodThrows) {
            throw new IllegalStateException("Provoked exception.");
        }
    }

    public void setAfterMethodThrows(final boolean afterMethodThrows) {
        this.afterMethodThrows = afterMethodThrows;
    }
}
