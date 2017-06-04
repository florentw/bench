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
package io.amaze.bench.shared.jgroups;

import com.google.common.annotations.VisibleForTesting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Created on 10/8/16.
 */
public class JgroupsViewMultiplexer {

    private static final Logger log = LogManager.getLogger();

    // Stores the listener along with its last seen view for diff purposes
    private final Map<JgroupsViewListener, View> viewListeners = new HashMap<>();

    private final JChannel jChannel;

    public JgroupsViewMultiplexer(@NotNull final JChannel jChannel) {
        this.jChannel = requireNonNull(jChannel);
    }

    public void addListener(@NotNull final JgroupsViewListener viewListener) {
        requireNonNull(viewListener);
        log.debug("Adding view listener {}...", viewListener);

        synchronized (viewListeners) {
            if (viewListeners.containsKey(viewListener)) {
                throw new IllegalStateException("View listener is already registered.");
            }
        }

        View initialView = jChannel.view();
        viewListener.initialView(initialView.getMembers());

        synchronized (viewListeners) {
            viewListeners.put(viewListener, initialView);
        }
    }

    public void removeListener(@NotNull final JgroupsViewListener viewListener) {
        requireNonNull(viewListener);
        log.debug("Removing view listener {}...", viewListener);

        synchronized (viewListeners) {
            viewListeners.remove(viewListener);
        }
    }

    public void viewUpdate(final View newView) {
        requireNonNull(newView);
        log.debug("Processing view update {}...", newView);

        Map<JgroupsViewListener, Address[][]> diffs = new HashMap<>();

        synchronized (viewListeners) {
            viewListeners.forEach((viewListener, currentView) -> {
                Address[][] diff = View.diff(currentView, newView);
                viewListeners.put(viewListener, newView);
                diffs.put(viewListener, diff);
            });
        }

        diffs.forEach((viewListener, diff) -> {
            for (Address joined : diff[0]) {
                viewListener.memberJoined(joined);
            }
            for (Address left : diff[1]) {
                viewListener.memberLeft(left);
            }
        });
    }

    @VisibleForTesting
    Map<JgroupsViewListener, View> getViewListeners() {
        return viewListeners;
    }
}
