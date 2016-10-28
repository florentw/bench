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

    private final Map<String, Set<JgroupsListener<? extends Serializable>>> listeners = new HashMap<>();

    public <T extends Serializable> void addListener(@NotNull final Class<T> inputMessageType,
                                                     @NotNull final JgroupsListener<T> listener) {
        checkNotNull(inputMessageType);
        checkNotNull(listener);
        String msgClassName = inputMessageType.getName();

        synchronized (listeners) {
            Set<JgroupsListener<? extends Serializable>> listenersByKey = listeners.get(msgClassName);
            if (listenersByKey == null) {
                listenersByKey = new HashSet<>();
            } else if (listenersByKey.contains(listener)) {
                throw new IllegalStateException("This listener is already registered for " + msgClassName);
            }

            listenersByKey.add(listener);
            listeners.put(msgClassName, listenersByKey);
        }
    }

    public void dispatch(@NotNull final Message msg) {
        checkNotNull(msg);

        listenersFor(msg).forEach(listener -> {
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
            listeners.entrySet().forEach(entry -> {
                if (!entry.getValue().remove(listener)) {
                    log.warn("Tried to remove an non-existent listener for {}, {}", entry.getKey(), listener);
                } else {
                    log.debug("Removing listener for {}, {}", entry.getKey(), listener);
                }
            });
        }
    }

    @VisibleForTesting
    Map<String, Set<JgroupsListener<? extends Serializable>>> getListeners() { // NOSONAR
        return listeners;
    }

    private Set<JgroupsListener<? extends Serializable>> listenersFor(final Message msg) {
        checkNotNull(msg);
        String msgClassName = msg.getObject().getClass().getName();
        synchronized (listeners) {
            Set<JgroupsListener<? extends Serializable>> targetListeners = listeners.get(msgClassName); // NOSONAR
            if (targetListeners == null) {
                log.debug("No listeners for {}, message {}", msgClassName, msg);
                return Collections.emptySet();
            }
            return new HashSet<>(targetListeners);
        }
    }
}
