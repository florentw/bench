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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Message;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 9/30/16.
 */
public class JgroupsListenerMultiplexer {

    private static final Logger log = LogManager.getLogger();

    private final Map<Class<? extends Serializable>, Set<JgroupsListener<? extends Serializable>>> listeners = new HashMap<>();

    public <T extends Serializable> void addListener(@NotNull final Class<T> inputMessageType,
                                                     @NotNull final JgroupsListener<T> listener) {
        checkNotNull(inputMessageType);
        checkNotNull(listener);

        synchronized (listeners) {
            if (listeners.containsKey(inputMessageType)) {
                throw new IllegalStateException("A listener is already registered for " + inputMessageType.getName());
            }
            Set<JgroupsListener<? extends Serializable>> listenersByKey = listeners.get(inputMessageType);
            if (listenersByKey == null) {
                listenersByKey = new HashSet<>();
            }

            listenersByKey.add(listener);
            listeners.put(inputMessageType, listenersByKey);
        }
    }

    public void dispatch(final Message msg) {
        checkNotNull(msg);

        Set<JgroupsListener<? extends Serializable>> targetListeners = listenersFor(msg);
        targetListeners.forEach(listener -> {
            try {
                listener.onMessage(msg, msg.getObject());
            } catch (Exception e) {
                log.warn("Error while processing message {}, listener is {}", msg, listener, e);
            }
        });
    }

    public void removeListener(@NotNull final JgroupsListener<? extends Serializable> listener) {
        checkNotNull(listener);
        synchronized (listeners) {
            listeners.values().forEach(listenersForKey -> {
                if (!listenersForKey.remove(listener)) {
                    log.warn("Tried to remove an non-existent listener for {}", listener);
                }
            });
        }
    }

    @VisibleForTesting
    Map<Class<? extends Serializable>, Set<JgroupsListener<? extends Serializable>>> getListeners() { // NOSONAR
        return listeners;
    }

    private Set<JgroupsListener<? extends Serializable>> listenersFor(final Message msg) {
        checkNotNull(msg);
        synchronized (listeners) {
            Set<JgroupsListener<? extends Serializable>> targetListeners = listeners.get(msg.getObject().getClass()); // NOSONAR
            if (targetListeners == null) {
                log.debug("No listeners for message {}", msg);
                return Collections.emptySet();
            }
            return new HashSet<>(targetListeners);
        }
    }
}
