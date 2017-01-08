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

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 10/2/16.
 */
public class JgroupsStateMultiplexer {

    private static final Logger log = LogManager.getLogger();

    private final Map<JgroupsStateKey, JgroupsStateHolder<? extends Serializable>> stateHolderObjects = new HashMap<>();

    public void addStateHolder(@NotNull final JgroupsStateHolder<?> holder) {
        checkNotNull(holder);
        JgroupsStateKey key = holder.getKey();

        log.debug("Adding state holder {}...", key);

        synchronized (stateHolderObjects) {
            if (stateHolderObjects.containsKey(key)) {
                throw new IllegalStateException("A holder for the key " + key + " is already registered.");
            }
            stateHolderObjects.put(key, holder);
        }
    }

    public void removeStateHolder(@NotNull final JgroupsStateKey key) {
        checkNotNull(key);

        log.debug("Removing state holder for {}...", key);

        synchronized (stateHolderObjects) {
            stateHolderObjects.remove(key);
        }
    }

    public void gatherStateFrom(@NotNull final OutputStream output) throws IOException {
        checkNotNull(output);

        log.debug("Gathering state from holders...");

        try (ObjectOutputStream oos = new ObjectOutputStream(output)) {
            JgroupsSharedState sharedState = gatherStateFromHolders();
            oos.writeObject(sharedState);
        }
    }

    public void writeStateTo(@NotNull final InputStream input) throws IOException {
        checkNotNull(input);

        log.debug("Writing state to holders...");

        try (ObjectInputStream ois = new ObjectInputStream(input)) {
            JgroupsSharedState newState = (JgroupsSharedState) ois.readObject();
            setNewStateOnHolders(newState);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Error while de-serializing.", e);
        }
    }

    @VisibleForTesting
    Map<JgroupsStateKey, JgroupsStateHolder<? extends Serializable>> getStateHolderObjects() {
        return stateHolderObjects;
    }

    @SuppressWarnings("unchecked")
    private void setNewStateOnHolders(final JgroupsSharedState newState) {
        synchronized (stateHolderObjects) {
            for (Map.Entry<JgroupsStateKey, Serializable> entry : newState.getStatesByKey().entrySet()) {
                JgroupsStateHolder<Serializable> holder = //
                        (JgroupsStateHolder<Serializable>) stateHolderObjects.get(entry.getKey()); // NOSONAR
                if (holder == null) {
                    continue;
                }

                holder.setState(entry.getValue());
            }
        }
    }

    private JgroupsSharedState gatherStateFromHolders() {
        Collection<JgroupsStateHolder<?>> stateHoldersCopy;
        synchronized (stateHolderObjects) {
            stateHoldersCopy = new HashSet<>(stateHolderObjects.values());
        }
        Map<JgroupsStateKey, Serializable> statesByKey = new HashMap<>(stateHoldersCopy.size());
        for (JgroupsStateHolder holder : stateHoldersCopy) {
            statesByKey.put(holder.getKey(), holder.getState());
        }
        return new JgroupsSharedState(statesByKey);
    }

}
