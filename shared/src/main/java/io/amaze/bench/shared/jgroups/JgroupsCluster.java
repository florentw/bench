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
package io.amaze.bench.shared.jgroups;

import com.google.common.annotations.VisibleForTesting;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/2/16.
 */
public final class JgroupsCluster extends ReceiverAdapter {

    private final JgroupsListenerMultiplexer listenerMultiplexer;
    private final JgroupsStateMultiplexer stateMultiplexer;

    public JgroupsCluster() {
        this(new JgroupsListenerMultiplexer(), new JgroupsStateMultiplexer());
    }

    @VisibleForTesting
    JgroupsCluster(@NotNull final JgroupsListenerMultiplexer listenerMultiplexer,
                   @NotNull final JgroupsStateMultiplexer stateMultiplexer) {
        this.listenerMultiplexer = checkNotNull(listenerMultiplexer);
        this.stateMultiplexer = checkNotNull(stateMultiplexer);
    }

    @Override
    public void receive(@NotNull final Message msg) {
        checkNotNull(msg);
        listenerMultiplexer.dispatch(msg);
    }

    @Override
    public void getState(@NotNull final OutputStream output) throws IOException {
        checkNotNull(output);
        stateMultiplexer.gatherStateFrom(output);
    }

    @Override
    public void setState(@NotNull final InputStream input) throws IOException {
        checkNotNull(input);
        stateMultiplexer.writeStateTo(input);
    }

    public JgroupsListenerMultiplexer listenerMultiplexer() {
        return listenerMultiplexer;
    }

    public JgroupsStateMultiplexer stateMultiplexer() {
        return stateMultiplexer;
    }
}
