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
import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/2/16.
 */
public final class JgroupsClusterMember extends ReceiverAdapter {

    @VisibleForTesting
    static final String CLUSTER_NAME = "bench-cluster";
    private static final Logger log = LogManager.getLogger();
    private static final int DEFAULT_STATE_TIMEOUT_MS = 10000;

    private final JChannel jChannel;
    private final JgroupsListenerMultiplexer listenerMultiplexer;
    private final JgroupsStateMultiplexer stateMultiplexer;
    private final JgroupsViewMultiplexer viewMultiplexer;

    public JgroupsClusterMember(final JChannel jChannel) {
        this(jChannel, //
             new JgroupsListenerMultiplexer(), //
             new JgroupsStateMultiplexer(), //
             new JgroupsViewMultiplexer(jChannel));
    }

    @VisibleForTesting
    JgroupsClusterMember(@NotNull final JChannel jChannel,
                         @NotNull final JgroupsListenerMultiplexer listenerMultiplexer,
                         @NotNull final JgroupsStateMultiplexer stateMultiplexer,
                         @NotNull final JgroupsViewMultiplexer viewMultiplexer) {
        this.jChannel = checkNotNull(jChannel);
        this.listenerMultiplexer = checkNotNull(listenerMultiplexer);
        this.stateMultiplexer = checkNotNull(stateMultiplexer);
        this.viewMultiplexer = checkNotNull(viewMultiplexer);
    }

    public synchronized void join() {
        log.info("Member joining cluster...");

        jChannel.receiver(this);
        try {
            jChannel.connect(CLUSTER_NAME);
            jChannel.getState(null, DEFAULT_STATE_TIMEOUT_MS);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
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

    @Override
    public void viewAccepted(final View view) {
        checkNotNull(view);
        viewMultiplexer.viewUpdate(view);
    }

    public JgroupsListenerMultiplexer listenerMultiplexer() {
        return listenerMultiplexer;
    }

    public JgroupsStateMultiplexer stateMultiplexer() {
        return stateMultiplexer;
    }

    public JgroupsViewMultiplexer viewMultiplexer() {
        return viewMultiplexer;
    }
}
