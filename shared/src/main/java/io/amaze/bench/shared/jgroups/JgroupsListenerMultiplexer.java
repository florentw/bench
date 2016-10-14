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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 9/30/16.
 */
public class JgroupsListenerMultiplexer {

    private static final Logger log = LogManager.getLogger();

    private final Map<Class<? extends Serializable>, JgroupsListener<? extends Serializable>> listeners = new HashMap<>();

    public <T extends Serializable> void addListener(@NotNull final Class<T> inputMessageType,
                                                     @NotNull final JgroupsListener<T> listener) {
        checkNotNull(inputMessageType);
        checkNotNull(listener);

        synchronized (listeners) {
            if (listeners.containsKey(inputMessageType)) {
                throw new IllegalStateException("A listener is already registered for " + inputMessageType.getName());
            }
            listeners.put(inputMessageType, listener);
        }
    }

    public void dispatch(final Message msg) {
        checkNotNull(msg);

        Optional<JgroupsListener> targetListener = listenerFor(msg);
        if (!targetListener.isPresent()) {
            return;
        }
        try {
            targetListener.get().onMessage(msg, msg.getObject());
        } catch (Exception e) {
            log.warn("Error while processing message {}, listener is {}", msg, targetListener.get(), e);
        }
    }

    public void removeListenerFor(@NotNull final Class<? extends Serializable> inputMessageType) {
        checkNotNull(inputMessageType);
        synchronized (listeners) {
            JgroupsListener listener = listeners.remove(inputMessageType);
            if (listener == null) {
                log.warn("Tried to remove an non-existent listener for {}", inputMessageType);
            }
        }
    }

    @VisibleForTesting
    Map<Class<? extends Serializable>, JgroupsListener<? extends Serializable>> getListeners() {
        return listeners;
    }

    private Optional<JgroupsListener> listenerFor(final Message msg) {
        checkNotNull(msg);
        synchronized (listeners) {
            JgroupsListener targetListener = listeners.get(msg.getObject().getClass());
            if (targetListener == null) {
                log.warn("Could not find a listener for message {}", msg);
                return Optional.empty();
            }
            return Optional.of(targetListener);
        }
    }

}
